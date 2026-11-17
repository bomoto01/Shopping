package com.shopping.service;

import com.shopping.dto.response.RecommendationResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.entity.*;
import com.shopping.enums.InteractionType;
import com.shopping.enums.ProductStatus;
import com.shopping.enums.UserStatus;
import com.shopping.mapper.ProductMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.RecommendationLogRepository;
import com.shopping.repository.UserInteractionRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @Mock
    private RecommendationLogRepository recommendationLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private User userWithPersonalization;
    private User userOptedOut;
    private Product product1;
    private Category category;

    @BeforeEach
    void setUp() {
        userWithPersonalization = User.builder()
                .userId(1L)
                .email("user1@shopping.vn")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(true)
                .build();

        userOptedOut = User.builder()
                .userId(2L)
                .email("user2@shopping.vn")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(false)
                .build();

        category = Category.builder()
                .categoryId(5L)
                .name("Thời Trang Nam")
                .build();

        product1 = Product.builder()
                .productId(10L)
                .name("Áo Polo Nam")
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("TC-10 & TC-09: Khi người dùng tắt cá nhân hóa -> Kích hoạt Fallback sản phẩm phổ biến")
    void getRecommendations_optOut_returnsFallback() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userOptedOut.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail(userOptedOut.getEmail())).thenReturn(Optional.of(userOptedOut));
        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product1)));

        ProductResponse productResponse = ProductResponse.builder()
                .productId(10L)
                .name("Áo Polo Nam")
                .build();
        when(productMapper.toProductResponse(product1)).thenReturn(productResponse);

        RecommendationResponse response = recommendationService.getRecommendations(5);

        assertNotNull(response);
        assertTrue(response.getIsFallback());
        assertEquals("FALLBACK-POPULAR-NO-AI", response.getAlgorithmVersion());
        assertEquals(1, response.getProducts().size());

        verify(recommendationLogRepository, times(1)).save(any(RecommendationLog.class));
    }

    @Test
    @DisplayName("Gợi ý AI thành công khi người dùng bật cá nhân hóa và có lịch sử tương tác")
    void getRecommendations_activePersonalization_returnsAi() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userWithPersonalization.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserInteraction interaction = UserInteraction.builder()
                .interactionId(1L)
                .user(userWithPersonalization)
                .product(product1)
                .interactionType(InteractionType.VIEW)
                .build();

        when(userRepository.findByEmail(userWithPersonalization.getEmail())).thenReturn(Optional.of(userWithPersonalization));
        when(userInteractionRepository.findTop50ByUser_UserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(interaction));
        when(productRepository.findByCategory_CategoryIdAndStatus(eq(5L), eq(ProductStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product1)));

        ProductResponse productResponse = ProductResponse.builder()
                .productId(10L)
                .name("Áo Polo Nam")
                .build();
        when(productMapper.toProductResponse(product1)).thenReturn(productResponse);

        RecommendationResponse response = recommendationService.getRecommendations(5);

        assertNotNull(response);
        assertFalse(response.getIsFallback());
        assertEquals("AI-COLLABORATIVE-V1", response.getAlgorithmVersion());
        assertEquals(1, response.getProducts().size());

        verify(recommendationLogRepository, times(1)).save(any(RecommendationLog.class));
    }

    @Test
    @DisplayName("TC-09: Khi truy vấn AI gặp sự cố -> Tự động Fallback sang sản phẩm phổ biến mà không crash")
    void getRecommendations_aiFails_gracefulFallback() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userWithPersonalization.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail(userWithPersonalization.getEmail())).thenReturn(Optional.of(userWithPersonalization));
        // Giả lập lỗi runtime khi đọc dữ liệu tương tác
        when(userInteractionRepository.findTop50ByUser_UserIdOrderByCreatedAtDesc(1L))
                .thenThrow(new RuntimeException("AI Service Timeout"));

        when(productRepository.findByStatus(eq(ProductStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product1)));

        ProductResponse productResponse = ProductResponse.builder()
                .productId(10L)
                .name("Áo Polo Nam")
                .build();
        when(productMapper.toProductResponse(product1)).thenReturn(productResponse);

        RecommendationResponse response = recommendationService.getRecommendations(5);

        assertNotNull(response);
        assertTrue(response.getIsFallback());
        assertEquals("FALLBACK-ON-ERROR", response.getAlgorithmVersion());
        assertEquals(1, response.getProducts().size());

        verify(recommendationLogRepository, times(1)).save(any(RecommendationLog.class));
    }
}
