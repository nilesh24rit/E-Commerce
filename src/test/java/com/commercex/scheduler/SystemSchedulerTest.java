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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSchedulerTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private SystemScheduler systemScheduler;

    @Test
    void cleanupExpiredCoupons_DeactivatesExpiredCoupons() {
        Coupon expiredCoupon = new Coupon();
        expiredCoupon.setActive(true);
        expiredCoupon.setValidUntil(LocalDateTime.now().minusDays(1));

        when(couponRepository.findAll()).thenReturn(List.of(expiredCoupon));

        systemScheduler.cleanupExpiredCoupons();

        assertFalse(expiredCoupon.isActive());
        verify(couponRepository).saveAll(anyList());
        assertNotNull(systemScheduler.getSchedulerStatus().get("ExpiredCouponCleanup"));
    }

    @Test
    void cleanupFailedPayments_MarksStalePendingAsFailed() {
        Payment stalePayment = new Payment();
        stalePayment.setPaymentStatus(PaymentStatus.PENDING);
        stalePayment.setCreatedAt(LocalDateTime.now().minusHours(25));

        when(paymentRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(List.of(stalePayment));

        systemScheduler.cleanupFailedPayments();

        assertEquals(PaymentStatus.FAILED, stalePayment.getPaymentStatus());
        verify(paymentRepository).saveAll(anyList());
        assertNotNull(systemScheduler.getSchedulerStatus().get("FailedPaymentCleanup"));
    }

    @Test
    void generateLowStockReport_ExecutesSuccessfully() {
        Product lowStockProduct = new Product();
        lowStockProduct.setName("Low Stock Item");
        lowStockProduct.setQuantity(3);
        lowStockProduct.setActive(true);

        when(productRepository.findAllByActiveTrue()).thenReturn(List.of(lowStockProduct));

        systemScheduler.generateLowStockReport();

        assertNotNull(systemScheduler.getSchedulerStatus().get("LowStockReport"));
    }

    @Test
    void generateDailySalesSummary_ExecutesSuccessfully() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.valueOf(150.00));

        when(orderRepository.findAll()).thenReturn(List.of(order));

        systemScheduler.generateDailySalesSummary();

        assertNotNull(systemScheduler.getSchedulerStatus().get("DailySalesSummary"));
    }

    @Test
    void generateWeeklyAnalyticsReport_ExecutesSuccessfully() {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now().minusDays(2));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(BigDecimal.valueOf(300.00));

        when(orderRepository.findAll()).thenReturn(List.of(order));

        systemScheduler.generateWeeklyAnalyticsReport();

        assertNotNull(systemScheduler.getSchedulerStatus().get("WeeklyAnalyticsReport"));
    }
}
