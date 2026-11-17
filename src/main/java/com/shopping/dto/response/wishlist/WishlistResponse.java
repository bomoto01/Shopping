package com.shopping.dto.response.wishlist;

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
public class WishlistResponse {

    private Long wishlistId;
    private Long productId;
    private String productName;
    private String productSlug;
    private String brand;
    private BigDecimal basePrice;
    private String imageUrl;
    private Boolean inStock;
    private Instant createdAt;
}
