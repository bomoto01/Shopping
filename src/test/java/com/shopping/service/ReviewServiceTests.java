package com.shopping.service;

import com.shopping.dto.request.CreateReviewRequest;
import com.shopping.dto.response.ProductRatingSummaryResponse;
import com.shopping.dto.response.ReviewResponse;
import com.shopping.entity.*;
import com.shopping.enums.OrderStatus;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ForbiddenException;
import com.shopping.mapper.ReviewMapper;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ReviewRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User customerUser;
    private User otherUser;
    private Product product;
    private ProductVariant variant;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .userId(1L)
                .email("customer@shopping.vn")
                .fullName("Nguyễn Khách Hàng")
                .status(UserStatus.ACTIVE)
                .build();

        otherUser = User.builder()
                .userId(2L)
                .email("other@shopping.vn")
                .fullName("Khách Khác")
                .status(UserStatus.ACTIVE)
                .build();

        product = Product.builder()
                .productId(10L)
                .name("Áo Sơ Mi Oxford")
                .build();

        variant = ProductVariant.builder()
                .variantId(100L)
                .product(product)
                .sku("SHIRT-WHT-L")
                .price(new BigDecimal("350000.00"))
                .build();

        order = Order.builder()
                .orderId(500L)
                .orderCode("ORD-20261117-XYZ123")
                .user(customerUser)
                .orderStatus(OrderStatus.DELIVERED)
                .build();

        orderItem = OrderItem.builder()
                .orderItemId(50L)
                .order(order)
                .variant(variant)
                .quantity(1)
                .unitPrice(new BigDecimal("350000.00"))
                .lineTotal(new BigDecimal("350000.00"))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customerUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Tạo đánh giá thành công khi đơn hàng đã DELIVERED và thuộc về user")
    void createReview_success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderItemId(50L)
                .rating(5)
                .comment("Áo đẹp, chất vải tốt!")
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));
        when(reviewRepository.existsByOrderItem_OrderItemId(50L)).thenReturn(false);

        Review savedReview = Review.builder()
                .reviewId(1L)
                .user(customerUser)
                .product(product)
                .orderItem(orderItem)
                .rating(5)
                .comment("Áo đẹp, chất vải tốt!")
                .isApproved(true)
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse responseDto = ReviewResponse.builder()
                .reviewId(1L)
                .productId(10L)
                .productName("Áo Sơ Mi Oxford")
                .rating(5)
                .comment("Áo đẹp, chất vải tốt!")
                .build();
        when(reviewMapper.toResponse(savedReview)).thenReturn(responseDto);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Áo đẹp, chất vải tốt!", response.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Tạo đánh giá thất bại khi đơn hàng chưa DELIVERED (ví dụ SHIPPING) -> ném BadRequestException")
    void createReview_notDelivered_throwsBadRequestException() {
        order.setOrderStatus(OrderStatus.SHIPPING);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderItemId(50L)
                .rating(5)
                .comment("Chưa nhận được áo nhưng đánh giá trước")
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reviewService.createReview(request));
        assertTrue(ex.getMessage().contains("DELIVERED"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Tạo đánh giá thất bại khi đơn hàng của người khác -> ném ForbiddenException")
    void createReview_notOwner_throwsForbiddenException() {
        order.setUser(otherUser);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderItemId(50L)
                .rating(4)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));

        assertThrows(ForbiddenException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("TC-13: Tạo đánh giá thất bại khi order item đã được đánh giá trước đó -> ném BadRequestException")
    void createReview_duplicateReview_throwsBadRequestException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderItemId(50L)
                .rating(5)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(orderItem));
        when(reviewRepository.existsByOrderItem_OrderItemId(50L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> reviewService.createReview(request));
        assertTrue(ex.getMessage().contains("đã đánh giá"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Lấy điểm đánh giá trung bình và tổng số đánh giá của sản phẩm")
    void getProductRatingSummary_success() {
        when(productRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.getAverageRatingByProductId(10L)).thenReturn(4.6666);
        when(reviewRepository.countByProduct_ProductIdAndIsApprovedTrue(10L)).thenReturn(12L);

        ProductRatingSummaryResponse summary = reviewService.getProductRatingSummary(10L);

        assertNotNull(summary);
        assertEquals(10L, summary.getProductId());
        assertEquals(4.7, summary.getAverageRating());
        assertEquals(12L, summary.getTotalReviews());
    }
}
