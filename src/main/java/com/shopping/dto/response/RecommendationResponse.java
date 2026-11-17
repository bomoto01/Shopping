package com.shopping.dto.response;

import com.shopping.dto.response.product.ProductResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    private List<ProductResponse> products;
    private String algorithmVersion;
    private Boolean isFallback;
    private Long responseTimeMs;
}
