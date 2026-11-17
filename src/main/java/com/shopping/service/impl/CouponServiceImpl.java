package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.coupon.CouponRequest;
import com.shopping.dto.response.coupon.CouponCheckResponse;
import com.shopping.dto.response.coupon.CouponResponse;
import com.shopping.entity.Coupon;
import com.shopping.enums.DiscountType;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.CouponMapper;
import com.shopping.repository.CouponRepository;
import com.shopping.service.interfaces.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons() {
        List<Coupon> coupons = couponRepository.findAvailableCoupons(Instant.now());
        return couponMapper.toResponseList(coupons);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponCheckResponse checkCoupon(String code, BigDecimal orderValue) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Mã giảm giá không được để trống");
        }
        if (orderValue == null || orderValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá trị đơn hàng không hợp lệ");
        }

        String cleanCode = code.trim().toUpperCase();
        Coupon coupon = couponRepository.findByCodeIgnoreCase(cleanCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá '" + cleanCode + "' không tồn tại"));

        validateCouponEligibility(coupon, orderValue);

        BigDecimal discountAmount = calculateDiscountAmount(coupon, orderValue);
        BigDecimal finalAmount = orderValue.subtract(discountAmount).max(BigDecimal.ZERO);

        return CouponCheckResponse.builder()
                .code(coupon.getCode())
                .valid(true)
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Áp dụng mã giảm giá thành công")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá", "id", id));
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> searchCoupons(String keyword, Boolean isActive, Pageable pageable) {
        Page<Coupon> page = couponRepository.searchCoupons(keyword, isActive, pageable);
        return PageResponse.from(page.map(couponMapper::toResponse));
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã giảm giá '" + cleanCode + "' đã tồn tại");
        }

        validateCouponConstraints(request);

        Coupon coupon = couponMapper.toEntity(request);
        coupon.setCode(cleanCode);
        coupon.setUsedCount(0);

        Coupon saved = couponRepository.save(coupon);
        log.info("Tạo mã giảm giá mới thành công: couponId={}, code={}", saved.getCouponId(), cleanCode);
        return couponMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá", "id", id));

        String cleanCode = request.getCode().trim().toUpperCase();
        if (!cleanCode.equalsIgnoreCase(coupon.getCode()) && couponRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã giảm giá '" + cleanCode + "' đã tồn tại");
        }

        validateCouponConstraints(request);

        couponMapper.updateEntityFromRequest(request, coupon);
        coupon.setCode(cleanCode);

        Coupon updated = couponRepository.save(coupon);
        log.info("Cập nhật mã giảm giá thành công: couponId={}, code={}", id, cleanCode);
        return couponMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public CouponResponse updateCouponStatus(Long id, boolean isActive) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá", "id", id));

        coupon.setIsActive(isActive);
        Coupon updated = couponRepository.save(coupon);
        log.info("Cập nhật trạng thái mã giảm giá: couponId={}, isActive={}", id, isActive);
        return couponMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá", "id", id));

        if (coupon.getUsedCount() != null && coupon.getUsedCount() > 0) {
            log.warn("Mã giảm giá đã phát sinh lượt sử dụng, chuyển trạng thái isActive = false thay vì xóa cứng: couponId={}", id);
            coupon.setIsActive(false);
            couponRepository.save(coupon);
            return;
        }

        couponRepository.delete(coupon);
        log.info("Xóa mã giảm giá thành công: couponId={}", id);
    }

    private void validateCouponConstraints(CouponRequest request) {
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new BadRequestException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Giá trị giảm theo phần trăm không thể vượt quá 100%");
            }
        }
    }

    private void validateCouponEligibility(Coupon coupon, BigDecimal orderValue) {
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new BadRequestException("Mã giảm giá hiện đang bị tạm khóa hoặc ngừng áp dụng");
        }

        Instant now = Instant.now();
        if (now.isBefore(coupon.getStartAt())) {
            throw new BadRequestException("Mã giảm giá chưa đến thời gian áp dụng");
        }

        // TC-06: Coupon hết hạn
        if (now.isAfter(coupon.getEndAt())) {
            throw new BadRequestException("Mã giảm giá đã hết hạn sử dụng");
        }

        // TC-07: Giá trị đơn hàng tối thiểu chưa đạt
        if (coupon.getMinOrderValue() != null && orderValue.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BadRequestException(String.format(
                    "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá này (Yêu cầu tối thiểu: %,.0f VNĐ, đơn hiện tại: %,.0f VNĐ)",
                    coupon.getMinOrderValue(), orderValue
            ));
        }

        // TC-08: Hết lượt sử dụng
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng");
        }
    }

    private BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal orderValue) {
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            return orderValue.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return coupon.getDiscountValue().min(orderValue);
        }
    }
}
