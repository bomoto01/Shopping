package com.shopping.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductResponse {

    private Long productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
