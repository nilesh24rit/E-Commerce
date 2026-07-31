package com.commercex.dto;

import com.commercex.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Order Number is required")
    private String orderNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
