package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.service.interfaces.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product - Public", description = "Các API duyệt, tìm kiếm, lọc và xem chi tiết sản phẩm công khai")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Tìm kiếm và lọc danh sách sản phẩm", description = "Lọc theo danh mục, từ khóa, khoảng giá, màu sắc, kích cỡ, thương hiệu và phân trang.")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "size", required = false) String size,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE), sort);

        PageResponse<ProductResponse> response = productService.getPublicProducts(
                keyword, categoryId, brand, minPrice, maxPrice, size, color, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết sản phẩm theo ID", description = "Trả về thông tin chi tiết quần áo, bộ ảnh và danh sách biến thể SKU còn mở bán.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable("id") Long id) {
        ProductDetailResponse response = productService.getPublicProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Xem chi tiết sản phẩm theo Slug", description = "Trả về chi tiết sản phẩm theo định danh đường dẫn URL thân thiện.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable("slug") String slug) {
        ProductDetailResponse response = productService.getPublicProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Lấy danh sách sản phẩm liên quan", description = "Trả về danh sách sản phẩm tương tự cùng danh mục.")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRelatedProducts(
            @PathVariable("id") Long id,
            @RequestParam(name = "limit", defaultValue = "6") int limit
    ) {
        List<ProductResponse> responses = productService.getRelatedProducts(id, limit);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
