package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.auth.LoginRequest;
import com.shopping.dto.request.auth.RefreshTokenRequest;
import com.shopping.dto.request.auth.RegisterRequest;
import com.shopping.dto.response.auth.AuthResponse;
import com.shopping.dto.response.auth.TokenRefreshResponse;
import com.shopping.dto.response.auth.UserSummaryResponse;
import com.shopping.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API xác thực tài khoản, JWT token và phiên đăng nhập")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản khách hàng mới", description = "Tạo mới tài khoản với vai trò mặc định ROLE_CUSTOMER.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Đăng ký tài khoản thành công", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực email và mật khẩu, phát hành Access Token và Refresh Token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Đăng nhập thành công", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng Refresh Token hợp lệ chưa bị thu hồi để nhận Access Token mới.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Làm mới Access Token thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất khỏi hệ thống", description = "Thu hồi Refresh Token trên server.")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Đăng xuất thành công", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Xem thông tin phiên đăng nhập hiện tại", description = "Trả về thông tin hồ sơ và vai trò của người dùng từ Access Token.")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getCurrentUser() {
        UserSummaryResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy thông tin tài khoản thành công", response));
    }
}
