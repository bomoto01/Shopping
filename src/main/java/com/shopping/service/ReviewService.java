package com.shopping.service;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CreateReviewRequest;
import com.shopping.dto.response.ProductRatingSummaryResponse;
import com.shopping.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request);

    PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    ProductRatingSummaryResponse getProductRatingSummary(Long productId);

    PageResponse<ReviewResponse> getMyReviews(Pageable pageable);

    PageResponse<ReviewResponse> getAllReviewsForAdmin(Pageable pageable);

    ReviewResponse toggleReviewApproval(Long reviewId, boolean isApproved);
}
