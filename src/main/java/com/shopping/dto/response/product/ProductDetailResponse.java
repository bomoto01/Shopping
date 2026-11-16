package com.shopping.dto.response.product;

import com.shopping.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;
    private CategoryResponse category;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private ProductStatus status;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    private Instant createdAt;
    private Instant updatedAt;
}
