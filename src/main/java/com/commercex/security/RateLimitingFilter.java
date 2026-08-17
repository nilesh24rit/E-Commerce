package com.commercex.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis-backed rate limiting filter for sensitive endpoints.
 * Applies sliding window rate limits to login, register, refresh, and
 * password-reset.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.login-max-requests:5}")
    private int loginMaxRequests;

    @Value("${app.rate-limit.login-window-seconds:60}")
    private int loginWindowSeconds;

    @Value("${app.rate-limit.register-max-requests:3}")
    private int registerMaxRequests;

    @Value("${app.rate-limit.register-window-seconds:60}")
    private int registerWindowSeconds;

    private static final Map<String, int[]> RATE_LIMIT_CONFIG = new ConcurrentHashMap<>();

    private static final String RATE_LIMIT_KEY_PREFIX = "rate:";

    // Endpoints subject to rate limiting: path -> [maxRequests, windowSeconds]
    private static final Map<String, String> RATE_LIMITED_PATHS = Map.of(
            "/api/auth/login", "login",
            "/api/auth/register", "register",
            "/api/auth/refresh", "login",
            "/api/auth/password-reset/request", "register");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String limitType = RATE_LIMITED_PATHS.get(path);

        if (limitType == null || !request.getMethod().equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String redisKey = RATE_LIMIT_KEY_PREFIX + limitType + ":" + clientIp;

        int maxRequests = "login".equals(limitType) ? loginMaxRequests : registerMaxRequests;
        int windowSeconds = "login".equals(limitType) ? loginWindowSeconds : registerWindowSeconds;

        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            if (count != null && count > maxRequests) {
                log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
                sendRateLimitResponse(response, windowSeconds);
                return;
            }
        } catch (Exception e) {
            // If Redis is unavailable, fail open (allow request) but log the issue
            log.error("Rate limiting Redis error, failing open: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void sendRateLimitResponse(HttpServletResponse response, int retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                        "\"message\":\"Rate limit exceeded. Please try again later.\"," +
                        "\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
