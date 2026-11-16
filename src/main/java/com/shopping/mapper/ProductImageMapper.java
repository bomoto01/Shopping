package com.shopping.mapper;

import com.shopping.dto.request.product.ProductImageRequest;
import com.shopping.dto.response.product.ProductImageResponse;
import com.shopping.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageResponse toProductImageResponse(ProductImage image);

    List<ProductImageResponse> toProductImageResponseList(List<ProductImage> images);

    @Mapping(target = "imageId", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toProductImage(ProductImageRequest request);
}
