package com.shopping.dto.response;

import com.shopping.enums.InteractionType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteractionResponse {

    private Long interactionId;
    private Long userId;
    private Long productId;
    private String productName;
    private InteractionType interactionType;
    private Double interactionValue;
    private String sessionId;
    private Instant createdAt;
}
