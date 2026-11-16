package com.shopping.controller.admin;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.request.product.ProductCreateRequest;
import com.shopping.dto.request.product.ProductImageRequest;
import com.shopping.dto.request.product.ProductUpdateRequest;
import com.shopping.dto.request.product.ProductVariantRequest;
import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.dto.response.product.ProductImageResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.dto.response.product.ProductVariantResponse;
import com.shopping.enums.ProductStatus;
import com.shopping.service.interfaces.ProductImageService;
import com.shopping.service.interfaces.ProductService;
import com.shopping.service.interfaces.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Product Management", description = "Các API quản lý sản phẩm, biến thể SKU và bộ sưu tập ảnh dành riêng cho Quản trị viên")
public class AdminProductController {

    private final ProductService productService;
    private final ProductVariantService variantService;
    private final ProductImageService imageService;

    @GetMapping
    @Operation(summary = "Xem danh sách sản phẩm quản trị", description = "Tìm kiếm theo từ khóa, lọc theo danh mục, thương hiệu, trạng thái và phân trang.")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAdminProducts(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "brand", required = false) String brand,
            @RequestParam(name = "status", required = false) ProductStatus status,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE), sort);

        PageResponse<ProductResponse> response = productService.getAdminProducts(keyword, categoryId, brand, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết sản phẩm quản trị", description = "Lấy đầy đủ thông tin sản phẩm kể cả sản phẩm ngừng hoạt động.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getAdminProductById(@PathVariable("id") Long id) {
        ProductDetailResponse response = productService.getAdminProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Tạo mới sản phẩm", description = "Tạo sản phẩm cùng các biến thể SKU và ảnh ban đầu nếu có.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo sản phẩm thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin sản phẩm", description = "Chỉnh sửa tên, danh mục, thương hiệu, giá cơ sở và mô tả.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductDetailResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật sản phẩm thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái sản phẩm", description = "Chuyển đổi trạng thái ACTIVE / INACTIVE của sản phẩm.")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProductStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") ProductStatus status
    ) {
        ProductDetailResponse response = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm sản phẩm", description = "Chuyển trạng thái sản phẩm sang INACTIVE.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa sản phẩm thành công", null));
    }

    // ==================== Biến thể SKU ====================

    @GetMapping("/{id}/variants")
    @Operation(summary = "Lấy danh sách biến thể SKU của sản phẩm", description = "Trả về tất cả biến thể màu sắc, kích cỡ, tồn kho của sản phẩm.")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariants(@PathVariable("id") Long id) {
        List<ProductVariantResponse> responses = variantService.getVariantsByProductId(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{id}/variants")
    @Operation(summary = "Thêm biến thể SKU mới cho sản phẩm", description = "Tạo SKU mới kèm màu sắc, kích cỡ, đơn giá và số lượng tồn ban đầu.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        ProductVariantResponse response = variantService.createVariant(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thêm biến thể thành công", response));
    }

    @PutMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Cập nhật thông tin biến thể SKU", description = "Chỉnh sửa màu sắc, kích cỡ, giá bán hoặc tồn kho.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable("id") Long id,
            @PathVariable("variantId") Long variantId,
            @Valid @RequestBody ProductVariantRequest request
    ) {
        ProductVariantResponse response = variantService.updateVariant(id, variantId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật biến thể thành công", response));
    }

    @PatchMapping("/{id}/variants/{variantId}/status")
    @Operation(summary = "Bật/Tắt trạng thái hoạt động của biến thể", description = "Thay đổi cờ isActive của SKU.")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariantStatus(
            @PathVariable("id") Long id,
            @PathVariable("variantId") Long variantId,
            @RequestParam("isActive") boolean isActive
    ) {
        ProductVariantResponse response = variantService.updateVariantStatus(id, variantId, isActive);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật trạng thái biến thể thành công", response));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @Operation(summary = "Ngừng hoạt động biến thể SKU", description = "Chuyển isActive của biến thể thành false.")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable("id") Long id,
            @PathVariable("variantId") Long variantId
    ) {
        variantService.deleteVariant(id, variantId);
        return ResponseEntity.ok(ApiResponse.success(200, "Vô hiệu hóa biến thể thành công", null));
    }

    // ==================== Ảnh sản phẩm ====================

    @GetMapping("/{id}/images")
    @Operation(summary = "Lấy danh sách ảnh sản phẩm", description = "Trả về bộ sưu tập ảnh sắp xếp theo thứ tự hiển thị.")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImages(@PathVariable("id") Long id) {
        List<ProductImageResponse> responses = imageService.getImagesByProductId(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Thêm ảnh mới cho sản phẩm", description = "Thêm đường dẫn ảnh, tự động đặt làm ảnh chính nếu là ảnh đầu tiên.")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductImageRequest request
    ) {
        ProductImageResponse response = imageService.addImage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thêm ảnh thành công", response));
    }

    @PatchMapping("/{id}/images/{imageId}/primary")
    @Operation(summary = "Đặt ảnh làm ảnh đại diện chính", description = "Đặt ảnh được chọn làm ảnh hiển thị chính của sản phẩm.")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setPrimaryImage(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId
    ) {
        ProductImageResponse response = imageService.setPrimaryImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success(200, "Đặt ảnh chính thành công", response));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Xóa ảnh sản phẩm", description = "Xóa ảnh khỏi sản phẩm, tự động chuyển ảnh chính nếu xóa ảnh chính hiện tại.")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId
    ) {
        imageService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa ảnh thành công", null));
    }
}
