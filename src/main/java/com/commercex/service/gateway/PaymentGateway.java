package com.commercex.service.gateway;

import com.commercex.entity.Payment;

public interface PaymentGateway {
    Payment createPayment(Payment payment);
    Payment verifyPayment(Payment payment);
    Payment refundPayment(Payment payment, String reason);
    Payment cancelPayment(Payment payment);
}
