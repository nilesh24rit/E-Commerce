package com.commercex.service;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.GoogleOAuthRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface GoogleOAuthService {

    AuthResponse authenticateWithGoogle(GoogleOAuthRequest request, HttpServletRequest httpRequest);
}
