package com.shopping.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long orderItemId;
    private Long variantId;
    private String productNameSnapshot;
    private String skuSnapshot;
    private String sizeSnapshot;
    private String colorSnapshot;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
