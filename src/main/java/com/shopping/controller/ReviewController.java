package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CreateReviewRequest;
import com.shopping.dto.response.ProductRatingSummaryResponse;
import com.shopping.dto.response.ReviewResponse;
import com.shopping.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "APIs đánh giá sản phẩm cho khách hàng")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/v1/reviews")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Đánh giá sản phẩm đã mua thành công (yêu cầu đơn hàng DELIVERED và mỗi order item chỉ đánh giá 1 lần)")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Đánh giá sản phẩm thành công", response));
    }

    @GetMapping("/api/v1/products/{productId}/reviews")
    @Operation(summary = "Xem danh sách đánh giá của sản phẩm (công khai, đã được phê duyệt)")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ReviewResponse> response = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách đánh giá thành công", response));
    }

    @GetMapping("/api/v1/products/{productId}/reviews/summary")
    @Operation(summary = "Xem điểm đánh giá trung bình và tổng số lượt đánh giá của sản phẩm")
    public ResponseEntity<ApiResponse<ProductRatingSummaryResponse>> getProductRatingSummary(
            @PathVariable Long productId) {
        ProductRatingSummaryResponse response = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy điểm đánh giá thành công", response));
    }

    @GetMapping("/api/v1/reviews/my")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Xem danh sách các đánh giá của tôi")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ReviewResponse> response = reviewService.getMyReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy lịch sử đánh giá của bạn thành công", response));
    }
}
