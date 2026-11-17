package com.shopping.service;

import com.shopping.dto.request.ClientInteractionRequest;
import com.shopping.dto.response.UserInteractionResponse;
import com.shopping.entity.User;
import com.shopping.enums.InteractionType;

public interface InteractionService {

    UserInteractionResponse recordClientInteraction(ClientInteractionRequest request);

    void recordInternalInteraction(User user, Long productId, InteractionType type, Double value);

    void deleteMyInteractions();
}
