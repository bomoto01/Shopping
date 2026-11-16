package com.shopping.controller.admin;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.request.user.UserStatusUpdateRequest;
import com.shopping.dto.response.user.UserProfileResponse;
import com.shopping.enums.UserStatus;
import com.shopping.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Các API quản trị tài khoản, danh sách người dùng và phân quyền (Dành riêng cho ADMIN)")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lấy danh sách người dùng hệ thống", description = "Tìm kiếm theo từ khóa (tên, email, SĐT), lọc theo trạng thái và phân trang.")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getAllUsers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) UserStatus status,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        PageResponse<UserProfileResponse> response = userService.getAllUsers(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái tài khoản", description = "Khóa hoặc mở khóa tài khoản người dùng (Thu hồi toàn bộ Refresh Token nếu bị khóa).")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái người dùng thành công", response));
    }
}
