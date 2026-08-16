package com.commercex.scheduler;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduler {

    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final Map<String, LocalDateTime> lastRunTimes = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredCoupons() {
        log.info("Running Expired Coupon Cleanup Job at {}", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(c -> c.isActive() && c.getValidUntil() != null && c.getValidUntil().isBefore(now))
                .collect(Collectors.toList());

        for (Coupon coupon : expiredCoupons) {
            coupon.setActive(false);
        }
        if (!expiredCoupons.isEmpty()) {
            couponRepository.saveAll(expiredCoupons);
            log.info("Deactivated {} expired coupons", expiredCoupons.size());
        }
        lastRunTimes.put("ExpiredCouponCleanup", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupFailedPayments() {
        log.info("Running Payment Cleanup Job at {}", LocalDateTime.now());
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);
        List<Payment> stalePayments = pendingPayments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        for (Payment payment : stalePayments) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        if (!stalePayments.isEmpty()) {
            paymentRepository.saveAll(stalePayments);
            log.info("Marked {} stale pending payments as FAILED", stalePayments.size());
        }
        lastRunTimes.put("FailedPaymentCleanup", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void generateLowStockReport() {
        log.info("Running Inventory Low Stock Report Job at {}", LocalDateTime.now());
        List<Product> activeProducts = productRepository.findAllByActiveTrue();
        List<Product> lowStockProducts = activeProducts.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() <= 10)
                .collect(Collectors.toList());

        log.info("--- LOW STOCK REPORT --- Total Products Low on Stock: {}", lowStockProducts.size());
        for (Product p : lowStockProducts) {
            log.info("Product ID: {}, Name: {}, SKU: {}, Current Stock: {}", p.getId(), p.getName(), p.getSku(), p.getQuantity());
        }
        lastRunTimes.put("LowStockReport", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailySalesSummary() {
        log.info("Running Daily Sales Summary Job at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<Order> todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(today))
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("--- DAILY SALES SUMMARY ({}) --- Total Orders: {}, Total Revenue: ${}", today, todayOrders.size(), totalRevenue);
        lastRunTimes.put("DailySalesSummary", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 1 ? * SUN")
    public void generateWeeklyAnalyticsReport() {
        log.info("Running Weekly Analytics Report Job at {}", LocalDateTime.now());
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Order> weeklyOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(sevenDaysAgo))
                .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal totalSales = weeklyOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("--- WEEKLY ANALYTICS REPORT --- Orders: {}, Total Sales: ${}", weeklyOrders.size(), totalSales);
        lastRunTimes.put("WeeklyAnalyticsReport", LocalDateTime.now());
    }
    
    public Map<String, LocalDateTime> getSchedulerStatus() {
        return lastRunTimes;
    }
}
