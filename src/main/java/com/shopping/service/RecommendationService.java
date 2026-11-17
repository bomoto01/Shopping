package com.shopping.service;

import com.shopping.dto.response.RecommendationResponse;

public interface RecommendationService {

    RecommendationResponse getRecommendations(int limit);
}
