package com.commercex.auth;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.LoginRequest;
import com.commercex.dto.LogoutResponse;
import com.commercex.dto.RefreshTokenRequest;
import com.commercex.dto.RegisterRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.RefreshToken;
import com.commercex.entity.User;
import com.commercex.exception.UserAlreadyExistsException;
import com.commercex.security.JwtTokenProvider;
import com.commercex.service.RefreshTokenService;
import com.commercex.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private UserResponse userResponse;
    private RefreshToken refreshToken;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("mock-old-token");

        userResponse = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        user = new User();
        user.setId(userResponse.getId());
        user.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setToken("mock-refresh-token");
        refreshToken.setUser(user);
    }

    @Test
    void register_Success() {
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(userResponse);
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(eq(userResponse.getId()), anyString(), isNull(), isNull())).thenReturn(refreshToken);

        AuthResponse response = authenticationService.register(registerRequest, httpRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
    }

    @Test
    void login_Success() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("mock-access-token");
        
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(userService.getUserById(user.getId())).thenReturn(userResponse);
        when(refreshTokenService.createRefreshToken(eq(user.getId()), anyString(), isNull(), isNull())).thenReturn(refreshToken);

        AuthResponse response = authenticationService.login(loginRequest, httpRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
    }

    @Test
    void refreshToken_Success() {
        when(refreshTokenService.rotateRefreshToken(anyString(), anyString(), isNull(), isNull())).thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access-token");
        when(userService.getUserById(user.getId())).thenReturn(userResponse);

        AuthResponse response = authenticationService.refreshToken(refreshTokenRequest, httpRequest);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
    }

    @Test
    void logout_Success() {
        doNothing().when(refreshTokenService).revokeRefreshToken(anyString());
        
        LogoutResponse response = authenticationService.logout(refreshTokenRequest);
        
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }
}
