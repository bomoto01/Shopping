package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CreateReviewRequest;
import com.shopping.dto.response.ProductRatingSummaryResponse;
import com.shopping.dto.response.ReviewResponse;
import com.shopping.entity.OrderItem;
import com.shopping.entity.Product;
import com.shopping.entity.Review;
import com.shopping.entity.User;
import com.shopping.enums.OrderStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ForbiddenException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.exception.UnauthorizedException;
import com.shopping.mapper.ReviewMapper;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ReviewRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        User user = getCurrentAuthenticatedUser();

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng sản phẩm đơn hàng với ID: " + request.getOrderItemId()));

        // Kiểm tra quyền sở hữu đơn hàng
        if (!orderItem.getOrder().getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Bạn không thể đánh giá sản phẩm từ đơn hàng của người khác");
        }

        // Kiểm tra trạng thái đơn hàng phải là DELIVERED
        if (orderItem.getOrder().getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao thành công (DELIVERED). Trạng thái hiện tại: " + orderItem.getOrder().getOrderStatus());
        }

        // TC-13: Ràng buộc duy nhất, mỗi order_item chỉ được đánh giá 1 lần
        if (reviewRepository.existsByOrderItem_OrderItemId(request.getOrderItemId())) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này trong đơn hàng rồi");
        }

        Product product = orderItem.getVariant().getProduct();

        Review review = Review.builder()
                .user(user)
                .product(product)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .isApproved(true)
                .build();

        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Page<Review> reviewPage = reviewRepository.findByProduct_ProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);

        return PageResponse.<ReviewResponse>builder()
                .content(reviewMapper.toResponseList(reviewPage.getContent()))
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .first(reviewPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getProductRatingSummary(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        long totalCount = reviewRepository.countByProduct_ProductIdAndIsApprovedTrue(productId);

        double roundedAvg = 0.0;
        if (avgRating != null) {
            roundedAvg = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        return ProductRatingSummaryResponse.builder()
                .productId(productId)
                .averageRating(roundedAvg)
                .totalReviews(totalCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(Pageable pageable) {
        User user = getCurrentAuthenticatedUser();
        Page<Review> reviewPage = reviewRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

        return PageResponse.<ReviewResponse>builder()
                .content(reviewMapper.toResponseList(reviewPage.getContent()))
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .first(reviewPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAllReviewsForAdmin(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return PageResponse.<ReviewResponse>builder()
                .content(reviewMapper.toResponseList(reviewPage.getContent()))
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .first(reviewPage.isFirst())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse toggleReviewApproval(Long reviewId, boolean isApproved) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        review.setIsApproved(isApproved);
        Review updated = reviewRepository.save(review);
        return reviewMapper.toResponse(updated);
    }

    private User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Vui lòng đăng nhập để tiếp tục");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để tiếp tục"));
    }
}
