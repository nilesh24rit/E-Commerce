package com.commercex.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Business metrics counters for CommerceX.
 * Tracks key business KPIs via Micrometer / Prometheus.
 * Metrics are safe to expose (counts only, no sensitive data).
 */
@Getter
@Component
public class BusinessMetrics {

    private final Counter ordersCreated;
    private final Counter paymentsSuccessful;
    private final Counter paymentsFailed;
    private final Counter productSearchCount;
    private final Counter cartItemsAdded;
    private final Counter cartItemsRemoved;
    private final Counter couponApplications;
    private final Counter userRegistrations;
    private final Counter wishlistAdditions;
    private final Counter reviewsSubmitted;

    public BusinessMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("commercex.orders.created")
                .description("Total number of orders created")
                .register(registry);

        this.paymentsSuccessful = Counter.builder("commercex.payments.successful")
                .description("Total number of successful payments")
                .register(registry);

        this.paymentsFailed = Counter.builder("commercex.payments.failed")
                .description("Total number of failed payment attempts")
                .register(registry);

        this.productSearchCount = Counter.builder("commercex.product.searches")
                .description("Total number of product search queries")
                .register(registry);

        this.cartItemsAdded = Counter.builder("commercex.cart.items.added")
                .description("Total number of items added to cart")
                .register(registry);

        this.cartItemsRemoved = Counter.builder("commercex.cart.items.removed")
                .description("Total number of items removed from cart")
                .register(registry);

        this.couponApplications = Counter.builder("commercex.coupons.applied")
                .description("Total number of coupon applications")
                .register(registry);

        this.userRegistrations = Counter.builder("commercex.users.registered")
                .description("Total number of user registrations")
                .register(registry);

        this.wishlistAdditions = Counter.builder("commercex.wishlist.additions")
                .description("Total number of wishlist item additions")
                .register(registry);

        this.reviewsSubmitted = Counter.builder("commercex.reviews.submitted")
                .description("Total number of reviews submitted")
                .register(registry);
    }
}
