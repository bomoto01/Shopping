package com.shopping.service.impl;

import com.shopping.dto.request.ClientInteractionRequest;
import com.shopping.dto.response.UserInteractionResponse;
import com.shopping.entity.Product;
import com.shopping.entity.User;
import com.shopping.entity.UserInteraction;
import com.shopping.enums.InteractionType;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.exception.UnauthorizedException;
import com.shopping.mapper.UserInteractionMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserInteractionRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImpl implements InteractionService {

    private final UserInteractionRepository userInteractionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserInteractionMapper userInteractionMapper;

    @Override
    @Transactional
    public UserInteractionResponse recordClientInteraction(ClientInteractionRequest request) {
        // TC-11 Anti-Spoofing: Client chỉ được gửi VIEW hoặc CLICK
        if (request.getInteractionType() != InteractionType.VIEW && request.getInteractionType() != InteractionType.CLICK) {
            throw new BadRequestException("Hành vi tương tác không hợp lệ. Client chỉ được gửi VIEW hoặc CLICK");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));

        User user = getOptionalAuthenticatedUser();

        // TC-10 Privacy check: Nếu user đã tắt cá nhân hóa, bỏ qua không ghi nhận
        if (user != null && Boolean.FALSE.equals(user.getAllowPersonalization())) {
            log.info("Bỏ qua ghi nhận tương tác vì người dùng đã tắt cá nhân hóa AI: userId={}", user.getUserId());
            return UserInteractionResponse.builder()
                    .productId(product.getProductId())
                    .productName(product.getName())
                    .interactionType(request.getInteractionType())
                    .build();
        }

        UserInteraction interaction = UserInteraction.builder()
                .user(user)
                .product(product)
                .interactionType(request.getInteractionType())
                .interactionValue(request.getInteractionValue())
                .sessionId(request.getSessionId())
                .build();

        UserInteraction saved = userInteractionRepository.save(interaction);
        return userInteractionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void recordInternalInteraction(User user, Long productId, InteractionType type, Double value) {
        if (user == null || Boolean.FALSE.equals(user.getAllowPersonalization())) {
            return;
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return;
        }

        UserInteraction interaction = UserInteraction.builder()
                .user(user)
                .product(product)
                .interactionType(type)
                .interactionValue(value != null ? value : 1.0)
                .build();

        userInteractionRepository.save(interaction);
        log.debug("Ghi nhận tương tác nội bộ: userId={}, productId={}, type={}", user.getUserId(), productId, type);
    }

    @Override
    @Transactional
    public void deleteMyInteractions() {
        User user = getCurrentAuthenticatedUser();
        userInteractionRepository.deleteAllByUserId(user.getUserId());
        log.info("Đã xóa toàn bộ lịch sử tương tác cá nhân hóa của user: userId={}", user.getUserId());
    }

    private User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Vui lòng đăng nhập để tiếp tục");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để tiếp tục"));
    }

    private User getOptionalAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
