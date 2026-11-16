package com.shopping.service.interfaces;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.product.ProductCreateRequest;
import com.shopping.dto.request.product.ProductUpdateRequest;
import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.enums.ProductStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    PageResponse<ProductResponse> getPublicProducts(
            String keyword, Long categoryId, String brand,
            BigDecimal minPrice, BigDecimal maxPrice,
            String size, String color, Pageable pageable
    );

    ProductDetailResponse getPublicProductById(Long id);

    ProductDetailResponse getPublicProductBySlug(String slug);

    List<ProductResponse> getRelatedProducts(Long productId, int limit);

    PageResponse<ProductResponse> getAdminProducts(
            String keyword, Long categoryId, String brand,
            ProductStatus status, Pageable pageable
    );

    ProductDetailResponse getAdminProductById(Long id);

    ProductDetailResponse createProduct(ProductCreateRequest request);

    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);

    ProductDetailResponse updateProductStatus(Long id, ProductStatus status);

    void deleteProduct(Long id);
}
