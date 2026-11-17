package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.response.ReviewResponse;
import com.shopping.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Admin", description = "APIs quản lý và kiểm duyệt đánh giá cho Admin & Staff")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Xem toàn bộ đánh giá trong hệ thống (kèm phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getAllReviews(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ReviewResponse> response = reviewService.getAllReviewsForAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách đánh giá thành công", response));
    }

    @PutMapping("/{reviewId}/approval")
    @Operation(summary = "Duyệt hoặc ẩn đánh giá (isApproved = true/false)")
    public ResponseEntity<ApiResponse<ReviewResponse>> toggleReviewApproval(
            @PathVariable Long reviewId,
            @RequestParam boolean isApproved) {
        ReviewResponse response = reviewService.toggleReviewApproval(reviewId, isApproved);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cập nhật trạng thái duyệt thành công", response));
    }
}
