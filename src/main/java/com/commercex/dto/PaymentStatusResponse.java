package com.commercex.dto;

import com.commercex.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatusResponse {
    private String paymentId;
    private PaymentStatus paymentStatus;
    private String failureReason;
}
