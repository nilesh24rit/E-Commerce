package com.commercex.service.impl;

import com.commercex.entity.RefreshToken;
import com.commercex.entity.User;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.exception.TokenRefreshException;
import com.commercex.repository.RefreshTokenRepository;
import com.commercex.repository.UserRepository;
import com.commercex.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refresh-expiration-ms:86400000}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId, String deviceId, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .deviceIdentifier(deviceId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        log.info("Created new refresh token for user: {} on device: {}", user.getEmail(), deviceId);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh Token not found in database"));
    }

    @Override
    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (refreshToken.isRevoked()) {
            log.warn("Attempted to use revoked refresh token: {}", token);
            throw new TokenRefreshException("Refresh token was revoked. Please sign in again.");
        }

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("Refresh token expired and was deleted: {}", token);
            throw new TokenRefreshException("Refresh token was expired. Please make a new sign in request.");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenStr, String deviceId, String ipAddress, String userAgent) {
        RefreshToken oldToken = validateRefreshToken(oldTokenStr);

        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        log.info("Revoked old refresh token during rotation for user: {}", oldToken.getUser().getEmail());

        // Create new token
        return createRefreshToken(oldToken.getUser().getId(), deviceId, ipAddress, userAgent);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token explicitly revoked for user: {}", refreshToken.getUser().getEmail());
    }

    @Override
    @Transactional
    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshTokenRepository.delete(refreshToken);
        log.info("Refresh token deleted for user: {}", refreshToken.getUser().getEmail());
    }

    @Override
    @Transactional
    public void deleteAllUserTokens(User user) {
        int count = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted all {} refresh tokens for user: {}", count, user.getEmail());
    }

    @Override
    @Transactional
    public int cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired refresh tokens from database.", deletedCount);
        }
        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveSessions(User user) {
        return refreshTokenRepository.findAllByUserAndRevokedFalseAndExpiryDateAfter(user, Instant.now());
    }
}
