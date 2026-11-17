package com.shopping.mapper;

import com.shopping.dto.response.inventory.InventoryTransactionResponse;
import com.shopping.dto.response.inventory.StockSummaryResponse;
import com.shopping.entity.InventoryTransaction;
import com.shopping.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(target = "variantId", source = "variant.variantId")
    @Mapping(target = "sku", source = "variant.sku")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "previousStock", ignore = true)
    @Mapping(target = "currentStock", source = "variant.stockQuantity")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);

    List<InventoryTransactionResponse> toResponseList(List<InventoryTransaction> transactions);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    StockSummaryResponse toStockSummaryResponse(ProductVariant variant);

    List<StockSummaryResponse> toStockSummaryResponseList(List<ProductVariant> variants);
}
