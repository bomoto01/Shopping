package com.shopping.mapper;

import com.shopping.dto.response.cart.CartItemResponse;
import com.shopping.dto.response.cart.CartResponse;
import com.shopping.entity.Cart;
import com.shopping.entity.CartItem;
import com.shopping.entity.Product;
import com.shopping.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "variantId", source = "variant.variantId")
    @Mapping(target = "productId", source = "variant.product.productId")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "productSlug", source = "variant.product.slug")
    @Mapping(target = "sku", source = "variant.sku")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "unitPrice", source = "variant.price")
    @Mapping(target = "maxStock", source = "variant.stockQuantity")
    @Mapping(target = "inStock", expression = "java(Boolean.TRUE.equals(item.getVariant().getIsActive()) && item.getVariant().getStockQuantity() > 0)")
    @Mapping(target = "lineTotal", expression = "java(item.getVariant().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    @Mapping(target = "imageUrl", source = "item.variant.product", qualifiedByName = "getPrimaryProductImage")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    @Named("getPrimaryProductImage")
    default String getPrimaryProductImage(Product product) {
        if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().get(0).getImageUrl());
    }

    default CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return CartResponse.builder()
                    .items(List.of())
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        List<CartItemResponse> items = toItemResponseList(cart.getItems());
        int totalQuantity = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(items)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }
}
