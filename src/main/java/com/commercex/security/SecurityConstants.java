package com.commercex.security;

public class SecurityConstants {
    public static final String[] PUBLIC_URLS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/password-reset/request",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
    public static final String[] PUBLIC_GET_URLS = {
            "/api/categories/**",
            "/api/categories",
            "/api/products/**",
            "/api/products"
    };
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}
