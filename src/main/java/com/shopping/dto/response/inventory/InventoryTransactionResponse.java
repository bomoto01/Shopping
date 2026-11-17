package com.shopping.dto.response.inventory;

import com.shopping.enums.InventoryReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    private Long transactionId;
    private Long variantId;
    private String sku;
    private String productName;
    private Integer quantityChange;
    private Integer previousStock;
    private Integer currentStock;
    private String reason;
    private InventoryReferenceType referenceType;
    private Long referenceId;
    private String createdByName;
    private Instant createdAt;
}
