package com.shopping.service.interfaces;

import com.shopping.dto.request.auth.LoginRequest;
import com.shopping.dto.request.auth.RefreshTokenRequest;
import com.shopping.dto.request.auth.RegisterRequest;
import com.shopping.dto.response.auth.AuthResponse;
import com.shopping.dto.response.auth.TokenRefreshResponse;
import com.shopping.dto.response.auth.UserSummaryResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshTokenString);

    UserSummaryResponse getCurrentUser();
}
