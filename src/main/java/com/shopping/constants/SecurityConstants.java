package com.shopping.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ROLE_CLAIM = "roles";
    public static final String USER_ID_CLAIM = "userId";

    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/api/v1/products/**",
            "/api/v1/categories/**",
            "/api/v1/recommendations/**",
            "/api/v1/interactions",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info",
            "/api/v1/health"
    };
}
