package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.ClientInteractionRequest;
import com.shopping.dto.response.UserInteractionResponse;
import com.shopping.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
@Tag(name = "User Interactions", description = "APIs thu thập hành vi tương tác phục vụ AI Recommendation")
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    @Operation(summary = "Ghi nhận tương tác từ phía Client (TC-11 Anti-Spoofing: CHỈ CHẤP NHẬN 'VIEW' hoặc 'CLICK')")
    public ResponseEntity<ApiResponse<UserInteractionResponse>> recordClientInteraction(
            @Valid @RequestBody ClientInteractionRequest request) {
        UserInteractionResponse response = interactionService.recordClientInteraction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Ghi nhận tương tác thành công", response));
    }

    @DeleteMapping("/my")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Xóa toàn bộ lịch sử tương tác cá nhân của tôi (Tuân thủ quyền riêng tư dữ liệu cá nhân)")
    public ResponseEntity<ApiResponse<Void>> deleteMyInteractions() {
        interactionService.deleteMyInteractions();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Đã xóa toàn bộ lịch sử tương tác cá nhân thành công", null));
    }
}
