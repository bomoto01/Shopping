package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.response.coupon.CouponCheckResponse;
import com.shopping.dto.response.coupon.CouponResponse;
import com.shopping.service.interfaces.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon - Public/Customer", description = "Các API tra cứu và kiểm tra mã giảm giá dành cho khách hàng")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/active")
    @Operation(summary = "Lấy danh sách mã giảm giá khả dụng", description = "Trả về các mã giảm giá đang trong thời gian hiệu lực và còn lượt dùng.")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons() {
        List<CouponResponse> responses = couponService.getAvailableCoupons();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/check")
    @Operation(summary = "Kiểm tra tính hợp lệ và số tiền giảm của mã giảm giá", description = "Kiểm tra mã giảm giá với giá trị đơn hàng dự kiến.")
    public ResponseEntity<ApiResponse<CouponCheckResponse>> checkCoupon(
            @RequestParam("code") String code,
            @RequestParam("orderValue") BigDecimal orderValue
    ) {
        CouponCheckResponse response = couponService.checkCoupon(code, orderValue);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
