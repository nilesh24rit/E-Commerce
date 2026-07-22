package com.commercex.service.impl;

import com.commercex.entity.RefreshToken;
import com.commercex.entity.User;
import com.commercex.exception.TokenRefreshException;
import com.commercex.repository.RefreshTokenRepository;
import com.commercex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(10000));
        refreshToken.setRevoked(false);
    }

    @Test
    void createRefreshToken_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken created = refreshTokenService.createRefreshToken(user.getId(), "device1", "127.0.0.1", "agent");

        assertNotNull(created);
        assertEquals(user, created.getUser());
        assertEquals("device1", created.getDeviceIdentifier());
        assertFalse(created.isRevoked());
    }

    @Test
    void validateRefreshToken_Success() {
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        RefreshToken validated = refreshTokenService.validateRefreshToken(refreshToken.getToken());
        assertNotNull(validated);
    }

    @Test
    void validateRefreshToken_Expired_ThrowsException() {
        refreshToken.setExpiryDate(Instant.now().minusMillis(1000));
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.validateRefreshToken(refreshToken.getToken()));
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void validateRefreshToken_Revoked_ThrowsException() {
        refreshToken.setRevoked(true);
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.validateRefreshToken(refreshToken.getToken()));
    }

    @Test
    void rotateRefreshToken_Success() {
        when(refreshTokenRepository.findByToken(refreshToken.getToken())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        
        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

        RefreshToken rotated = refreshTokenService.rotateRefreshToken(refreshToken.getToken(), "device2", "ip", "agent");

        assertTrue(refreshToken.isRevoked()); // old token must be revoked
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // one for revoke, one for new
    }
}
