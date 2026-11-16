package com.shopping.service.interfaces;

import com.shopping.dto.request.product.ProductVariantRequest;
import com.shopping.dto.response.product.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {

    List<ProductVariantResponse> getVariantsByProductId(Long productId);

    ProductVariantResponse getVariantById(Long variantId);

    ProductVariantResponse createVariant(Long productId, ProductVariantRequest request);

    ProductVariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request);

    ProductVariantResponse updateVariantStatus(Long productId, Long variantId, boolean isActive);

    void deleteVariant(Long productId, Long variantId);
}
