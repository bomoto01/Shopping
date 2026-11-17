package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.response.RecommendationResponse;
import com.shopping.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI Recommendations", description = "APIs gợi ý sản phẩm thông minh tích hợp AI & Fallback")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm gợi ý cá nhân hóa (Tự động kích hoạt Fallback khi chưa đăng nhập, tắt AI hoặc AI quá tải)")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        RecommendationResponse response = recommendationService.getRecommendations(limit);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách gợi ý sản phẩm thành công", response));
    }
}
