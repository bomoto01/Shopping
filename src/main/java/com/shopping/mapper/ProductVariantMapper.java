package com.shopping.mapper;

import com.shopping.dto.request.product.ProductVariantRequest;
import com.shopping.dto.response.product.ProductVariantResponse;
import com.shopping.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    ProductVariantResponse toProductVariantResponse(ProductVariant variant);

    List<ProductVariantResponse> toProductVariantResponseList(List<ProductVariant> variants);

    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest request);

    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "product", ignore = true)
    void updateVariantFromRequest(ProductVariantRequest request, @MappingTarget ProductVariant variant);
}
