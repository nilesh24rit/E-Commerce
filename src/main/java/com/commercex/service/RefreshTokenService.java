package com.commercex.service;

import com.commercex.entity.RefreshToken;
import com.commercex.entity.User;
import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId, String deviceId, String ipAddress, String userAgent);
    RefreshToken findByToken(String token);
    RefreshToken validateRefreshToken(String token);
    RefreshToken rotateRefreshToken(String oldToken, String deviceId, String ipAddress, String userAgent);
    void revokeRefreshToken(String token);
    void deleteRefreshToken(String token);
    void deleteAllUserTokens(User user);
    int cleanupExpiredTokens();
    List<RefreshToken> getActiveSessions(User user);
}
