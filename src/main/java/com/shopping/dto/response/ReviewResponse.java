package com.shopping.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long productId;
    private String productName;
    private Long orderItemId;
    private Long userId;
    private String userFullName;
    private Integer rating;
    private String comment;
    private Boolean isApproved;
    private Instant createdAt;
}
