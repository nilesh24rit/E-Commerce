package com.commercex.scheduling;

import com.commercex.entity.Coupon;
import com.commercex.entity.Order;
import com.commercex.entity.Payment;
import com.commercex.entity.Product;
import com.commercex.entity.enums.OrderStatus;
import com.commercex.entity.enums.PaymentStatus;
import com.commercex.repository.CouponRepository;
import com.commercex.repository.OrderRepository;
import com.commercex.repository.PaymentRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scheduling {

    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final Map<String, LocalDateTime> lastRunTimes = new ConcurrentHashMap<>();

    @Value("${app.scheduling.low-stock-threshold:10}")
    private int lowStockThreshold;

    /**
     * Daily cleanup of expired coupons at midnight.
     */
    @Scheduled(cron = "${app.scheduling.coupon-cleanup-cron:0 0 0 * * ?}")
    @Transactional
    public void cleanupExpiredCoupons() {
        log.info("Starting automated Expired Coupon Cleanup Job at {}", LocalDateTime.now());
        log.trace("Scanning repository for active expired coupons");

        LocalDateTime now = LocalDateTime.now();
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(c -> c.isActive() && c.getValidUntil() != null && c.getValidUntil().isBefore(now))
                .collect(Collectors.toList());

        if (expiredCoupons.isEmpty()) {
            log.debug("No expired active coupons found to deactivate");
        } else {
            for (Coupon coupon : expiredCoupons) {
                coupon.setActive(false);
                log.debug("Deactivating expired coupon: {}", coupon.getCode());
            }
            couponRepository.saveAll(expiredCoupons);
            log.info("Successfully deactivated {} expired coupons", expiredCoupons.size());
        }
        lastRunTimes.put("ExpiredCouponCleanup", LocalDateTime.now());
    }

    /**
     * Stale pending payment cleanup every night at 2:00 AM.
     */
    @Scheduled(cron = "${app.scheduling.payment-cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupFailedPayments() {
        log.info("Starting automated Stale Pending Payment Cleanup Job at {}", LocalDateTime.now());
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        log.debug("Cutoff time for stale payments: {}", cutoff);

        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);
        List<Payment> stalePayments = pendingPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        if (stalePayments.isEmpty()) {
            log.debug("No stale pending payments found past 24 hours threshold");
        } else {
            for (Payment payment : stalePayments) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                log.debug("Marking stale payment {} as FAILED", payment.getId());
            }
            paymentRepository.saveAll(stalePayments);
            log.warn("Marked {} stale pending payments as FAILED", stalePayments.size());
        }
        lastRunTimes.put("FailedPaymentCleanup", LocalDateTime.now());
    }

    /**
     * Daily low inventory stock report at 8:00 AM.
     */
    @Scheduled(cron = "${app.scheduling.low-stock-cron:0 0 8 * * ?}")
    public void generateLowStockReport() {
        log.info("Starting automated Inventory Low Stock Report Job at {}", LocalDateTime.now());
        log.debug("Low stock threshold configured at: {}", lowStockThreshold);

        List<Product> activeProducts = productRepository.findAllByActiveTrue();
        List<Product> lowStockProducts = activeProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() <= lowStockThreshold)
                .collect(Collectors.toList());

        if (lowStockProducts.isEmpty()) {
            log.info("All products have healthy stock levels (above threshold {})", lowStockThreshold);
        } else {
            log.warn("--- LOW STOCK ALERT --- Total Products Low on Stock: {}", lowStockProducts.size());
            for (Product p : lowStockProducts) {
                log.warn("Low stock alert for Product ID: {}, Name: '{}', SKU: '{}', Remaining Quantity: {}",
                        p.getId(), p.getName(), p.getSku(), p.getQuantity());
            }
        }
        lastRunTimes.put("LowStockReport", LocalDateTime.now());
    }

    /**
     * Daily sales summary calculation at 11:00 PM.
     */
    @Scheduled(cron = "${app.scheduling.sales-summary-cron:0 0 23 * * ?}")
    public void generateDailySalesSummary() {
        log.info("Starting automated Daily Sales Summary Job at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();

        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(today))
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("=== DAILY SALES SUMMARY ({}) === Total Orders: {}, Total Revenue: ${}",
                today, todayOrders.size(), totalRevenue);
        lastRunTimes.put("DailySalesSummary", LocalDateTime.now());
    }

    /**
     * Weekly analytics report on Sundays at 1:00 AM.
     */
    @Scheduled(cron = "${app.scheduling.weekly-report-cron:0 0 1 ? * SUN}")
    public void generateWeeklyAnalyticsReport() {
        log.info("Starting automated Weekly Analytics Report Job at {}", LocalDateTime.now());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Order> weeklyOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(sevenDaysAgo))
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalSales = weeklyOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("=== WEEKLY ANALYTICS REPORT === Orders Count: {}, Total Sales: ${}",
                weeklyOrders.size(), totalSales);
        lastRunTimes.put("WeeklyAnalyticsReport", LocalDateTime.now());
    }

    /**
     * Purge expired refresh tokens daily at 3:30 AM.
     */
    @Scheduled(cron = "${app.scheduling.token-cleanup-cron:0 30 3 * * ?}")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        log.info("Starting automated Refresh Token Cleanup Job at {}", LocalDateTime.now());
        try {
            refreshTokenRepository.deleteByExpiryDateBefore(java.time.Instant.now());
            log.info("Successfully purged expired refresh tokens");
        } catch (Exception ex) {
            log.error("Error during refresh token cleanup job", ex);
        }
        lastRunTimes.put("RefreshTokenCleanup", LocalDateTime.now());
    }

    public Map<String, LocalDateTime> getSchedulerStatus() {
        return lastRunTimes;
    }
}
