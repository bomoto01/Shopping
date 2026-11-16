package com.shopping.service.interfaces;

import com.shopping.dto.request.product.ProductImageRequest;
import com.shopping.dto.response.product.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    List<ProductImageResponse> getImagesByProductId(Long productId);

    ProductImageResponse addImage(Long productId, ProductImageRequest request);

    ProductImageResponse setPrimaryImage(Long productId, Long imageId);

    void deleteImage(Long productId, Long imageId);
}
