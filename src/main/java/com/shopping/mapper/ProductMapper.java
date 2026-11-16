package com.shopping.mapper;

import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ProductImageMapper.class, ProductVariantMapper.class})
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "primaryImageUrl", source = "product", qualifiedByName = "getPrimaryImageUrl")
    @Mapping(target = "inStock", source = "product", qualifiedByName = "checkInStock")
    ProductResponse toProductResponse(Product product);

    ProductDetailResponse toProductDetailResponse(Product product);

    @Named("getPrimaryImageUrl")
    default String getPrimaryImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl());
    }

    @Named("checkInStock")
    default Boolean checkInStock(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return false;
        }
        return product.getVariants().stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getIsActive()) && v.getStockQuantity() != null && v.getStockQuantity() > 0);
    }
}
