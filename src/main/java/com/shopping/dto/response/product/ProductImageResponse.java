package com.shopping.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {

    private Long imageId;
    private String imageUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
}
