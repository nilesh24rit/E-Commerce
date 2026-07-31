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
public class PaymentHistoryResponse {
    private UUID id;
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
}
