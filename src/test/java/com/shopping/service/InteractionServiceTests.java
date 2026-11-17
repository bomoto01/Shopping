package com.shopping.service;

import com.shopping.dto.request.ClientInteractionRequest;
import com.shopping.dto.response.UserInteractionResponse;
import com.shopping.entity.Product;
import com.shopping.entity.User;
import com.shopping.entity.UserInteraction;
import com.shopping.enums.InteractionType;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.mapper.UserInteractionMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserInteractionRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.impl.InteractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTests {

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInteractionMapper userInteractionMapper;

    @InjectMocks
    private InteractionServiceImpl interactionService;

    private User userWithPersonalization;
    private User userOptedOut;
    private Product product;

    @BeforeEach
    void setUp() {
        userWithPersonalization = User.builder()
                .userId(1L)
                .email("user1@shopping.vn")
                .fullName("User One")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(true)
                .build();

        userOptedOut = User.builder()
                .userId(2L)
                .email("user2@shopping.vn")
                .fullName("User Two")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(false)
                .build();

        product = Product.builder()
                .productId(10L)
                .name("Quần Jeans Slim Fit")
                .build();
    }

    @Test
    @DisplayName("Ghi nhận tương tác VIEW từ Client thành công")
    void recordClientInteraction_view_success() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userWithPersonalization.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ClientInteractionRequest request = ClientInteractionRequest.builder()
                .productId(10L)
                .interactionType(InteractionType.VIEW)
                .interactionValue(20.0)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(userWithPersonalization.getEmail())).thenReturn(Optional.of(userWithPersonalization));

        UserInteraction saved = UserInteraction.builder()
                .interactionId(1L)
                .user(userWithPersonalization)
                .product(product)
                .interactionType(InteractionType.VIEW)
                .interactionValue(20.0)
                .build();
        when(userInteractionRepository.save(any(UserInteraction.class))).thenReturn(saved);

        UserInteractionResponse responseDto = UserInteractionResponse.builder()
                .interactionId(1L)
                .productId(10L)
                .interactionType(InteractionType.VIEW)
                .build();
        when(userInteractionMapper.toResponse(saved)).thenReturn(responseDto);

        UserInteractionResponse response = interactionService.recordClientInteraction(request);

        assertNotNull(response);
        assertEquals(InteractionType.VIEW, response.getInteractionType());
        verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
    }

    @Test
    @DisplayName("TC-11 Anti-Spoofing: Client gửi PURCHASE -> bị từ chối với BadRequestException")
    void recordClientInteraction_antiSpoofing_purchase_throwsBadRequestException() {
        ClientInteractionRequest request = ClientInteractionRequest.builder()
                .productId(10L)
                .interactionType(InteractionType.PURCHASE)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> interactionService.recordClientInteraction(request));

        assertTrue(ex.getMessage().contains("Client chỉ được gửi VIEW hoặc CLICK"));
        verify(userInteractionRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-11 Anti-Spoofing: Client gửi ADD_TO_CART -> bị từ chối với BadRequestException")
    void recordClientInteraction_antiSpoofing_addToCart_throwsBadRequestException() {
        ClientInteractionRequest request = ClientInteractionRequest.builder()
                .productId(10L)
                .interactionType(InteractionType.ADD_TO_CART)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> interactionService.recordClientInteraction(request));

        assertTrue(ex.getMessage().contains("Client chỉ được gửi VIEW hoặc CLICK"));
        verify(userInteractionRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-10 Privacy Opt-Out: Khi allowPersonalization = false -> hoàn toàn bỏ qua không lưu DB")
    void recordClientInteraction_privacyOptOut_skipsDbSave() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userOptedOut.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ClientInteractionRequest request = ClientInteractionRequest.builder()
                .productId(10L)
                .interactionType(InteractionType.VIEW)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(userOptedOut.getEmail())).thenReturn(Optional.of(userOptedOut));

        UserInteractionResponse response = interactionService.recordClientInteraction(request);

        assertNotNull(response);
        // Xác nhận tuyệt đối không lưu vào user_interactions
        verify(userInteractionRepository, never()).save(any());
    }
}
