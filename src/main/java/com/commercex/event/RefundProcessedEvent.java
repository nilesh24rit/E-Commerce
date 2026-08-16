package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RefundProcessedEvent {
    private UUID paymentId;
    private String userEmail;
    private BigDecimal amount;
    private String reason;
}
