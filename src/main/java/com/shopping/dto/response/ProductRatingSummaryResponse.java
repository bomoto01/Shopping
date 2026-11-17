package com.shopping.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummaryResponse {

    private Long productId;
    private Double averageRating;
    private Long totalReviews;
}
