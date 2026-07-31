package com.commercex.entity;

import com.commercex.entity.enums.PaymentMethod;
import com.commercex.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_payment_id", columnList = "payment_id", unique = true),
        @Index(name = "idx_payment_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(name = "payment_id", nullable = false, unique = true, updatable = false)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String gatewayName;

    private String gatewayTransactionId;
    
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    private String failureReason;
}
