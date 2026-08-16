package com.commercex.controller;

import com.commercex.auth.AuthenticationService;
import com.commercex.dto.ApiResponse;
import com.commercex.dto.AuthResponse;
import com.commercex.dto.LoginRequest;
import com.commercex.dto.LogoutResponse;
import com.commercex.dto.RefreshTokenRequest;
import com.commercex.dto.RegisterRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.User;
import com.commercex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for User Authentication and Session Management")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authenticationService.register(request, httpRequest);
        return new ResponseEntity<>(ApiResponse.success(response, "User registered successfully"), HttpStatus.CREATED);
    }

    @Operation(summary = "Login an existing user")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authenticationService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Refresh an access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authenticationService.refreshToken(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @Operation(summary = "Logout the current user session")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        LogoutResponse response = authenticationService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Logout successful"));
    }

    @Operation(summary = "Get current authenticated user details")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserResponse response = userService.getUserById(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Current user retrieved successfully"));
    }

    @Operation(summary = "Request password reset email")
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@org.springframework.web.bind.annotation.RequestParam String email) {
        userService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset instructions sent to your email"));
    }
}
