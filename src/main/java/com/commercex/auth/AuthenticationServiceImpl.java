package com.commercex.auth;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.LoginRequest;
import com.commercex.dto.LogoutResponse;
import com.commercex.dto.RefreshTokenRequest;
import com.commercex.dto.RegisterRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.RefreshToken;
import com.commercex.entity.User;
import com.commercex.security.CustomUserDetails;
import com.commercex.security.JwtTokenProvider;
import com.commercex.service.RefreshTokenService;
import com.commercex.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("User-Agent");
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        UserResponse userResponse = userService.createUser(request);
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userResponse.getId(),
                "Device", // Can be extended to accept device IDs from client
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("User login attempt with email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        User user = userService.findByEmail(request.getEmail());
        UserResponse userResponse = userService.getUserById(user.getId());
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(),
                "Device",
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        log.info("Refresh token requested for rotation");

        // 1. Validate and rotate the refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                request.getRefreshToken(),
                "Device",
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );

        User user = newRefreshToken.getUser();
        
        // 2. Generate new Access Token without hitting AuthenticationManager (since we don't have the password)
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        String newAccessToken = jwtTokenProvider.generateToken(authentication);
        UserResponse userResponse = userService.getUserById(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public LogoutResponse logout(RefreshTokenRequest request) {
        log.info("Logout requested");
        try {
            // Revoke rather than delete to maintain security audit trail
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        } catch (Exception ex) {
            log.warn("Logout attempted with invalid or missing token. Handled gracefully.");
        }
        
        return LogoutResponse.builder()
                .status(HttpStatus.OK.value())
                .message("Successfully logged out and session revoked.")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
