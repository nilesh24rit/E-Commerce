package com.commercex.auth;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.LoginRequest;
import com.commercex.dto.LogoutResponse;
import com.commercex.dto.RefreshTokenRequest;
import com.commercex.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);
    AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);
    LogoutResponse logout(RefreshTokenRequest request);
}
