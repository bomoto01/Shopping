package com.shopping.controller.admin;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.product.CategoryRequest;
import com.shopping.dto.response.product.CategoryResponse;
import com.shopping.service.interfaces.CategoryService;
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
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Category Management", description = "Các API quản lý danh mục sản phẩm dành riêng cho Quản trị viên")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Tạo mới danh mục sản phẩm", description = "Thêm danh mục mới, tự động tạo slug duy nhất.")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo danh mục thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật danh mục sản phẩm", description = "Chỉnh sửa tên danh mục, danh mục cha và cập nhật lại slug nếu đổi tên.")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable("id") Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật danh mục thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Kích hoạt hoặc vô hiệu hóa danh mục", description = "Thay đổi cờ isActive của danh mục.")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryStatus(
            @PathVariable("id") Long id,
            @RequestParam("isActive") boolean isActive
    ) {
        CategoryResponse response = categoryService.updateCategoryStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái danh mục thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục sản phẩm", description = "Xóa cứng danh mục nếu chưa có sản phẩm liên kết; chuyển sang isActive = false nếu đã có sản phẩm.")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa danh mục thành công", null));
    }
}
