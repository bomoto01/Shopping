package com.shopping.controller.admin;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.request.coupon.CouponRequest;
import com.shopping.dto.response.coupon.CouponResponse;
import com.shopping.service.interfaces.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Coupon Management", description = "Các API quản lý mã giảm giá, khuyến mãi dành riêng cho Quản trị viên")
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "Xem danh sách mã giảm giá", description = "Tìm kiếm theo mã code, lọc theo trạng thái hoạt động và phân trang.")
    public ResponseEntity<ApiResponse<PageResponse<CouponResponse>>> searchCoupons(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "isActive", required = false) Boolean isActive,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "sortBy", defaultValue = "startAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        PageResponse<CouponResponse> response = couponService.searchCoupons(keyword, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết mã giảm giá", description = "Lấy đầy đủ thông tin mã giảm giá theo ID.")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable("id") Long id) {
        CouponResponse response = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Tạo mới mã giảm giá", description = "Thêm mã giảm giá mới với các ràng buộc về ngày hiệu lực, giá trị đơn tối thiểu và số lượt dùng.")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo mã giảm giá thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật mã giảm giá", description = "Chỉnh sửa thông tin, giá trị và thời hạn mã giảm giá.")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable("id") Long id,
            @Valid @RequestBody CouponRequest request
    ) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật mã giảm giá thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Bật/Tắt trạng thái hoạt động của mã giảm giá", description = "Thay đổi cờ isActive của coupon.")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCouponStatus(
            @PathVariable("id") Long id,
            @RequestParam("isActive") boolean isActive
    ) {
        CouponResponse response = couponService.updateCouponStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mã giảm giá", description = "Xóa cứng nếu chưa từng sử dụng; chuyển isActive sang false nếu đã phát sinh lượt dùng.")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable("id") Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa mã giảm giá thành công", null));
    }
}
