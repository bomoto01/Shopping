package com.shopping.service.interfaces;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.coupon.CouponRequest;
import com.shopping.dto.response.coupon.CouponCheckResponse;
import com.shopping.dto.response.coupon.CouponResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    List<CouponResponse> getAvailableCoupons();

    CouponCheckResponse checkCoupon(String code, BigDecimal orderValue);

    CouponResponse getCouponById(Long id);

    PageResponse<CouponResponse> searchCoupons(String keyword, Boolean isActive, Pageable pageable);

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Long id, CouponRequest request);

    CouponResponse updateCouponStatus(Long id, boolean isActive);

    void deleteCoupon(Long id);
}
