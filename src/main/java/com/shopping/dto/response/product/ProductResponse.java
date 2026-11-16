package com.shopping.dto.response.product;

import com.shopping.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal basePrice;
    private ProductStatus status;
    private String primaryImageUrl;
    private Boolean inStock;
    private Instant createdAt;
}
