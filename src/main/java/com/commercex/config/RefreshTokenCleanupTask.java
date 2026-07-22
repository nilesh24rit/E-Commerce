package com.commercex.config;

import com.commercex.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private final RefreshTokenService refreshTokenService;

    // Run every day at 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens...");
        refreshTokenService.cleanupExpiredTokens();
        log.info("Finished scheduled cleanup of expired refresh tokens.");
    }
}
