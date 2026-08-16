package com.commercex.controller;

import com.commercex.dto.ApiResponse;
import com.commercex.scheduler.SystemScheduler;
import com.commercex.service.CategoryService;
import com.commercex.service.CouponService;
import com.commercex.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin System", description = "Admin APIs for Cache and Scheduler Management")
@SecurityRequirement(name = "bearerAuth")
public class AdminSystemController {

    private final CacheManager cacheManager;
    private final SystemScheduler systemScheduler;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CouponService couponService;

    @PostMapping("/cache/clear")
    @Operation(summary = "Clear all caches", description = "Clears products, categories, and coupons caches")
    public ResponseEntity<ApiResponse<String>> clearAllCaches() {
        log.info("Admin clearing all caches");
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok(ApiResponse.success(null, "All caches cleared successfully"));
    }

    @PostMapping("/cache/clear/{cacheName}")
    @Operation(summary = "Clear specific cache", description = "Clears a specific cache by name (e.g., products, categories, coupons)")
    public ResponseEntity<ApiResponse<String>> clearSpecificCache(@PathVariable String cacheName) {
        log.info("Admin clearing cache: {}", cacheName);
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok(ApiResponse.success(null, "Cache " + cacheName + " cleared successfully"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Cache " + cacheName + " not found"));
    }

    @PostMapping("/cache/refresh")
    @Operation(summary = "Refresh all caches", description = "Clears and repopulates all main caches (products, categories, coupons)")
    public ResponseEntity<ApiResponse<String>> refreshAllCaches() {
        log.info("Admin refreshing all caches");
        clearAllCaches();
        productService.getAllProducts();
        categoryService.getAllCategories();
        couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(null, "All caches refreshed successfully"));
    }

    @PostMapping("/cache/refresh/{cacheName}")
    @Operation(summary = "Refresh specific cache", description = "Clears and repopulates a specific cache by name")
    public ResponseEntity<ApiResponse<String>> refreshSpecificCache(@PathVariable String cacheName) {
        log.info("Admin refreshing cache: {}", cacheName);
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Cache " + cacheName + " not found"));
        }
        cache.clear();
        switch (cacheName.toLowerCase()) {
            case "products":
                productService.getAllProducts();
                break;
            case "categories":
                categoryService.getAllCategories();
                break;
            case "coupons":
                couponService.getAllCoupons();
                break;
            default:
                break;
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Cache " + cacheName + " refreshed successfully"));
    }

    @GetMapping("/scheduler/status")
    @Operation(summary = "View Scheduler Status", description = "Returns the last run times of background jobs")
    public ResponseEntity<ApiResponse<Map<String, LocalDateTime>>> getSchedulerStatus() {
        log.info("Admin fetching scheduler status");
        return ResponseEntity.ok(ApiResponse.success(systemScheduler.getSchedulerStatus(), "Scheduler status fetched"));
    }

    @PostMapping("/reports/trigger/{reportName}")
    @Operation(summary = "Trigger Report Manually", description = "Manually trigger a background report (low-stock, daily-sales, weekly-analytics)")
    public ResponseEntity<ApiResponse<String>> triggerReport(@PathVariable String reportName) {
        log.info("Admin manually triggering report: {}", reportName);
        switch (reportName.toLowerCase()) {
            case "low-stock":
                systemScheduler.generateLowStockReport();
                break;
            case "daily-sales":
                systemScheduler.generateDailySalesSummary();
                break;
            case "weekly-analytics":
                systemScheduler.generateWeeklyAnalyticsReport();
                break;
            case "cleanup-coupons":
                systemScheduler.cleanupExpiredCoupons();
                break;
            case "cleanup-payments":
                systemScheduler.cleanupFailedPayments();
                break;
            default:
                return ResponseEntity.badRequest().body(ApiResponse.error("Unknown report or job name"));
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Job " + reportName + " triggered successfully"));
    }
}
