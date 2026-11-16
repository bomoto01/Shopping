package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.response.product.CategoryResponse;
import com.shopping.service.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category - Public", description = "Các API xem danh mục sản phẩm công khai")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy cây danh mục sản phẩm đang hoạt động", description = "Trả về danh sách danh mục theo cấu trúc cha - con.")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategoriesTree() {
        List<CategoryResponse> responses = categoryService.getActiveCategoriesTree();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ danh mục sản phẩm đang hoạt động (dạng phẳng)", description = "Trả về danh sách phẳng các danh mục hoạt động.")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActiveCategories() {
        List<CategoryResponse> responses = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết một danh mục", description = "Lấy thông tin danh mục theo ID.")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable("id") Long id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
