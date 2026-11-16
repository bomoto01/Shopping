package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.user.ChangePasswordRequest;
import com.shopping.dto.request.user.UpdatePersonalizationRequest;
import com.shopping.dto.request.user.UpdateProfileRequest;
import com.shopping.dto.response.user.UserProfileResponse;
import com.shopping.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Profile", description = "Các API quản lý thông tin tài khoản, hồ sơ cá nhân và quyền riêng tư")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Xem chi tiết hồ sơ cá nhân", description = "Lấy toàn bộ thông tin tài khoản, cài đặt cá nhân hóa và sổ địa chỉ.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cập nhật họ tên, số điện thoại của tài khoản đang đăng nhập.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật hồ sơ cá nhân thành công", response));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Đổi mật khẩu tài khoản", description = "Xác minh mật khẩu cũ, cập nhật mật khẩu mới và thu hồi các phiên đăng nhập khác.")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại nếu cần.", null));
    }

    @PatchMapping("/me/personalization")
    @Operation(summary = "Cập nhật tùy chọn cá nhân hóa AI (Opt-out/Opt-in)", description = "Bật hoặc tắt tính năng ghi nhận tương tác và cá nhân hóa gợi ý AI.")
    public ResponseEntity<ApiResponse<Void>> updatePersonalization(@Valid @RequestBody UpdatePersonalizationRequest request) {
        userService.updatePersonalization(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật cài đặt cá nhân hóa thành công", null));
    }
}
