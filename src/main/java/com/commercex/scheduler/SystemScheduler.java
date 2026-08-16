package com.commercex.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduler {

    private final Map<String, LocalDateTime> lastRunTimes = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredCoupons() {
        log.info("Running Expired Coupon Cleanup Job at {}", LocalDateTime.now());
        lastRunTimes.put("ExpiredCouponCleanup", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupFailedPayments() {
        log.info("Running Payment Cleanup Job at {}", LocalDateTime.now());
        lastRunTimes.put("FailedPaymentCleanup", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void generateLowStockReport() {
        log.info("Running Inventory Low Stock Report Job at {}", LocalDateTime.now());
        lastRunTimes.put("LowStockReport", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailySalesSummary() {
        log.info("Running Daily Sales Summary Job at {}", LocalDateTime.now());
        lastRunTimes.put("DailySalesSummary", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 1 ? * SUN")
    public void generateWeeklyAnalyticsReport() {
        log.info("Running Weekly Analytics Report Job at {}", LocalDateTime.now());
        lastRunTimes.put("WeeklyAnalyticsReport", LocalDateTime.now());
    }
    
    public Map<String, LocalDateTime> getSchedulerStatus() {
        return lastRunTimes;
    }
}
