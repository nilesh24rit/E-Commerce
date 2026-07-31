package com.commercex.dto;

import com.commercex.entity.enums.PaymentMethod;
import com.commercex.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String gatewayName;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
