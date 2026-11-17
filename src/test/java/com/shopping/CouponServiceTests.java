package com.shopping;

import com.shopping.dto.request.coupon.CouponRequest;
import com.shopping.dto.response.coupon.CouponCheckResponse;
import com.shopping.dto.response.coupon.CouponResponse;
import com.shopping.entity.Coupon;
import com.shopping.enums.DiscountType;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.mapper.CouponMapper;
import com.shopping.repository.CouponRepository;
import com.shopping.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTests {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon validPercentCoupon;
    private Coupon validFixedCoupon;

    @BeforeEach
    void setUp() {
        validPercentCoupon = Coupon.builder()
                .couponId(1L)
                .code("SALE10")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10.0"))
                .minOrderValue(new BigDecimal("200000"))
                .startAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .endAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .usageLimit(100)
                .usedCount(10)
                .isActive(true)
                .build();

        validFixedCoupon = Coupon.builder()
                .couponId(2L)
                .code("GIAM50K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("300000"))
                .startAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .endAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .usageLimit(50)
                .usedCount(5)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Tạo coupon thất bại khi mã Code đã tồn tại")
    void testCreateCouponDuplicateCode() {
        CouponRequest request = CouponRequest.builder()
                .code("SALE10")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .startAt(Instant.now())
                .endAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("SALE10")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> couponService.createCoupon(request));
        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo coupon thất bại khi giá trị giảm theo % vượt quá 100%")
    void testCreateCouponPercentExceeds100() {
        CouponRequest request = CouponRequest.builder()
                .code("SALE150")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("150"))
                .startAt(Instant.now())
                .endAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("SALE150")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> couponService.createCoupon(request));
    }

    @Test
    @DisplayName("Tạo coupon thất bại khi thời gian kết thúc trước thời gian bắt đầu")
    void testCreateCouponEndAtBeforeStartAt() {
        CouponRequest request = CouponRequest.builder()
                .code("INVALIDDATE")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("30000"))
                .startAt(Instant.now().plus(5, ChronoUnit.DAYS))
                .endAt(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();

        when(couponRepository.existsByCodeIgnoreCase("INVALIDDATE")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> couponService.createCoupon(request));
    }

    @Test
    @DisplayName("TC-06: Áp dụng mã giảm giá thất bại khi mã đã hết hạn")
    void testCheckCouponExpired() {
        Coupon expiredCoupon = Coupon.builder()
                .code("EXPIRED")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("20"))
                .minOrderValue(BigDecimal.ZERO)
                .startAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .endAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .usageLimit(100)
                .usedCount(5)
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("EXPIRED")).thenReturn(Optional.of(expiredCoupon));

        assertThrows(BadRequestException.class, () -> couponService.checkCoupon("EXPIRED", new BigDecimal("500000")));
    }

    @Test
    @DisplayName("TC-07: Áp dụng mã giảm giá thất bại khi đơn hàng chưa đạt giá trị tối thiểu")
    void testCheckCouponBelowMinOrderValue() {
        when(couponRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(validPercentCoupon));

        // validPercentCoupon có minOrderValue = 200,000, kiểm tra đơn 150,000
        assertThrows(BadRequestException.class, () -> couponService.checkCoupon("SALE10", new BigDecimal("150000")));
    }

    @Test
    @DisplayName("TC-08: Áp dụng mã giảm giá thất bại khi đã hết lượt sử dụng")
    void testCheckCouponUsageLimitReached() {
        Coupon limitReachedCoupon = Coupon.builder()
                .code("LIMITREACHED")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .minOrderValue(BigDecimal.ZERO)
                .startAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .endAt(Instant.now().plus(5, ChronoUnit.DAYS))
                .usageLimit(50)
                .usedCount(50)
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCase("LIMITREACHED")).thenReturn(Optional.of(limitReachedCoupon));

        assertThrows(BadRequestException.class, () -> couponService.checkCoupon("LIMITREACHED", new BigDecimal("300000")));
    }

    @Test
    @DisplayName("Áp dụng mã giảm giá hợp lệ theo phần trăm và tính đúng số tiền giảm")
    void testCheckValidPercentCoupon() {
        when(couponRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(validPercentCoupon));

        CouponCheckResponse response = couponService.checkCoupon("SALE10", new BigDecimal("500000"));

        assertNotNull(response);
        assertTrue(response.getValid());
        assertEquals(new BigDecimal("50000.00"), response.getDiscountAmount()); // 10% của 500,000 là 50,000
        assertEquals(new BigDecimal("450000.00"), response.getFinalAmount()); // 500,000 - 50,000 = 450,000
    }

    @Test
    @DisplayName("Áp dụng mã giảm giá hợp lệ theo số tiền cố định và tính đúng số tiền giảm")
    void testCheckValidFixedCoupon() {
        when(couponRepository.findByCodeIgnoreCase("GIAM50K")).thenReturn(Optional.of(validFixedCoupon));

        CouponCheckResponse response = couponService.checkCoupon("GIAM50K", new BigDecimal("400000"));

        assertNotNull(response);
        assertTrue(response.getValid());
        assertEquals(new BigDecimal("50000"), response.getDiscountAmount());
        assertEquals(new BigDecimal("350000"), response.getFinalAmount()); // 400,000 - 50,000 = 350,000
    }
}
