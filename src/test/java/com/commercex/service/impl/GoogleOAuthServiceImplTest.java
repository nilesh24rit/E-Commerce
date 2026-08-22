package com.commercex.service.impl;

import com.commercex.dto.AuthResponse;
import com.commercex.dto.GoogleOAuthRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.RefreshToken;
import com.commercex.entity.Role;
import com.commercex.entity.User;
import com.commercex.entity.enums.RoleName;
import com.commercex.event.UserRegisteredEvent;
import com.commercex.mapper.UserMapper;
import com.commercex.repository.RoleRepository;
import com.commercex.repository.UserRepository;
import com.commercex.security.JwtTokenProvider;
import com.commercex.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private GoogleOAuthServiceImpl googleOAuthService;

    private GoogleOAuthRequest oauthRequest;
    private User existingUser;
    private Role customerRole;
    private RefreshToken mockRefreshToken;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        oauthRequest = GoogleOAuthRequest.builder()
                .idToken("valid-google-id-token")
                .email("user@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .pictureUrl("https://example.com/avatar.png")
                .build();

        customerRole = new Role(RoleName.ROLE_CUSTOMER);
        customerRole.setId(UUID.randomUUID());

        existingUser = User.builder()
                .email("user@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .password("encodedPassword")
                .enabled(true)
                .roles(new HashSet<>())
                .build();
        existingUser.setId(UUID.randomUUID());
        existingUser.getRoles().add(customerRole);

        mockRefreshToken = RefreshToken.builder()
                .token("mock-refresh-token")
                .user(existingUser)
                .build();

        userResponse = UserResponse.builder()
                .id(existingUser.getId())
                .email(existingUser.getEmail())
                .firstName("Jane")
                .lastName("Doe")
                .build();
    }

    @Test
    void authenticateWithGoogle_ExistingUser_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(eq(existingUser.getId()), anyString(), any(), any())).thenReturn(mockRefreshToken);
        when(userMapper.toDto(existingUser)).thenReturn(userResponse);

        AuthResponse response = googleOAuthService.authenticateWithGoogle(oauthRequest, httpRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("user@example.com", response.getUser().getEmail());

        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void authenticateWithGoogle_NewUser_RegistersAndLogsIn() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedRandomPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(any(UUID.class), anyString(), any(), any())).thenReturn(mockRefreshToken);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponse);

        AuthResponse response = googleOAuthService.authenticateWithGoogle(oauthRequest, httpRequest);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());

        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }
}
