package com.shopping.mapper;

import com.shopping.dto.response.wishlist.WishlistResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductImage;
import com.shopping.entity.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "brand", source = "product.brand")
    @Mapping(target = "basePrice", source = "product.basePrice")
    @Mapping(target = "inStock", source = "product", qualifiedByName = "isProductInStock")
    @Mapping(target = "imageUrl", source = "product", qualifiedByName = "getWishlistProductImage")
    WishlistResponse toResponse(Wishlist wishlist);

    List<WishlistResponse> toResponseList(List<Wishlist> wishlists);

    @Named("getWishlistProductImage")
    default String getWishlistProductImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl());
    }

    @Named("isProductInStock")
    default Boolean isProductInStock(Product product) {
        if (product == null || product.getVariants() == null) {
            return false;
        }
        return product.getVariants().stream()
                .anyMatch(v -> Boolean.TRUE.equals(v.getIsActive()) && v.getStockQuantity() != null && v.getStockQuantity() > 0);
    }
}
