package com.shopping.service.impl;

import com.shopping.dto.response.RecommendationResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.entity.Product;
import com.shopping.entity.RecommendationLog;
import com.shopping.entity.User;
import com.shopping.entity.UserInteraction;
import com.shopping.enums.ProductStatus;
import com.shopping.mapper.ProductMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.RecommendationLogRepository;
import com.shopping.repository.UserInteractionRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserInteractionRepository userInteractionRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RecommendationResponse getRecommendations(int limit) {
        long startTime = System.currentTimeMillis();
        int safeLimit = (limit <= 0 || limit > 50) ? 10 : limit;

        User user = getOptionalAuthenticatedUser();

        // Nếu người dùng không đăng nhập hoặc đã tắt tính năng cá nhân hóa AI -> Kích hoạt Fallback
        if (user == null || Boolean.FALSE.equals(user.getAllowPersonalization())) {
            return executeFallback(user, safeLimit, startTime, "FALLBACK-POPULAR-NO-AI");
        }

        try {
            // Lấy 50 tương tác gần nhất của người dùng để tìm các danh mục / sản phẩm ưa thích
            List<UserInteraction> interactions = userInteractionRepository.findTop50ByUser_UserIdOrderByCreatedAtDesc(user.getUserId());

            if (interactions.isEmpty()) {
                // Người dùng mới chưa có tương tác -> Fallback sang sản phẩm phổ biến
                return executeFallback(user, safeLimit, startTime, "FALLBACK-COLD-START");
            }

            // Thu thập các category ID mà người dùng tương tác nhiều nhất
            List<Long> preferredCategoryIds = interactions.stream()
                    .map(it -> it.getProduct().getCategory().getCategoryId())
                    .distinct()
                    .limit(3)
                    .collect(Collectors.toList());

            List<Product> recommendedProducts = new ArrayList<>();
            for (Long catId : preferredCategoryIds) {
                List<Product> catProducts = productRepository.findByCategory_CategoryIdAndStatus(
                        catId, ProductStatus.ACTIVE, PageRequest.of(0, safeLimit / preferredCategoryIds.size() + 1)
                ).getContent();
                recommendedProducts.addAll(catProducts);
            }

            // Loại bỏ sản phẩm trùng lặp và giới hạn đúng số lượng yêu cầu
            List<Product> distinctProducts = recommendedProducts.stream()
                    .distinct()
                    .limit(safeLimit)
                    .collect(Collectors.toList());

            if (distinctProducts.isEmpty()) {
                return executeFallback(user, safeLimit, startTime, "FALLBACK-EMPTY-PREDICTION");
            }

            long responseTime = System.currentTimeMillis() - startTime;

            // Ghi nhật ký gợi ý thành công từ AI/Collaborative
            logRecommendation(user, distinctProducts, "AI-COLLABORATIVE-V1", responseTime, false);

            List<ProductResponse> productResponses = distinctProducts.stream()
                    .map(productMapper::toProductResponse)
                    .collect(Collectors.toList());

            return RecommendationResponse.builder()
                    .products(productResponses)
                    .algorithmVersion("AI-COLLABORATIVE-V1")
                    .isFallback(false)
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception ex) {
            // TC-09: Tự động bắt lỗi ngoại lệ / timeout và fallback liền mạch
            log.warn("Lỗi khi tạo gợi ý AI cho user {}: {}. Kích hoạt chế độ dự phòng Fallback.", user.getUserId(), ex.getMessage());
            return executeFallback(user, safeLimit, startTime, "FALLBACK-ON-ERROR");
        }
    }

    private RecommendationResponse executeFallback(User user, int limit, long startTime, String reason) {
        List<Product> fallbackProducts = productRepository.findByStatus(ProductStatus.ACTIVE, PageRequest.of(0, limit)).getContent();
        long responseTime = System.currentTimeMillis() - startTime;

        logRecommendation(user, fallbackProducts, reason, responseTime, true);

        List<ProductResponse> productResponses = fallbackProducts.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return RecommendationResponse.builder()
                .products(productResponses)
                .algorithmVersion(reason)
                .isFallback(true)
                .responseTimeMs(responseTime)
                .build();
    }

    private void logRecommendation(User user, List<Product> products, String version, long responseTime, boolean isFallback) {
        String productIds = products.stream()
                .map(p -> String.valueOf(p.getProductId()))
                .collect(Collectors.joining(","));

        RecommendationLog logEntry = RecommendationLog.builder()
                .user(user)
                .recommendedProductIds(productIds)
                .algorithmVersion(version)
                .responseTimeMs(responseTime)
                .isFallback(isFallback)
                .build();

        recommendationLogRepository.save(logEntry);
    }

    private User getOptionalAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
