package com.shopping.service.interfaces;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.inventory.InventoryAdjustmentRequest;
import com.shopping.dto.response.inventory.InventoryTransactionResponse;
import com.shopping.dto.response.inventory.StockSummaryResponse;
import com.shopping.enums.InventoryReferenceType;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryTransactionResponse adjustStock(Long variantId, InventoryAdjustmentRequest request);

    PageResponse<InventoryTransactionResponse> getTransactions(
            Long variantId, InventoryReferenceType referenceType, Pageable pageable
    );

    PageResponse<StockSummaryResponse> getStockReport(
            String keyword, Boolean lowStockOnly, int lowStockThreshold, Pageable pageable
    );
}
