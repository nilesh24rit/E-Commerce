package com.commercex.service.gateway.impl;

import com.commercex.entity.Payment;
import com.commercex.entity.enums.PaymentStatus;
import com.commercex.service.gateway.PaymentGateway;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service("dummyPaymentGateway")
public class DummyPaymentGateway implements PaymentGateway {

    private final Random random = new Random();

    @Override
    public Payment createPayment(Payment payment) {
        payment.setGatewayTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        int chance = random.nextInt(100);
        if (chance < 70) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setGatewayResponse("{\"status\":\"SUCCESS\",\"message\":\"Transaction approved\"}");
        } else if (chance < 85) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setGatewayResponse("{\"status\":\"PENDING\",\"message\":\"Transaction pending from bank\"}");
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGatewayResponse("{\"status\":\"FAILED\",\"message\":\"Insufficient funds\"}");
            payment.setFailureReason("Insufficient funds or network error");
        }
        
        return payment;
    }

    @Override
    public Payment verifyPayment(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
            int chance = random.nextInt(100);
            if (chance < 80) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setGatewayResponse("{\"status\":\"SUCCESS\",\"message\":\"Transaction approved upon verification\"}");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setGatewayResponse("{\"status\":\"FAILED\",\"message\":\"Transaction failed upon verification\"}");
                payment.setFailureReason("Bank declined the transaction");
            }
        }
        return payment;
    }

    @Override
    public Payment refundPayment(Payment payment, String reason) {
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setGatewayResponse("{\"status\":\"REFUNDED\",\"reason\":\"" + reason + "\"}");
        return payment;
    }

    @Override
    public Payment cancelPayment(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setGatewayResponse("{\"status\":\"CANCELLED\",\"message\":\"User cancelled the transaction\"}");
        return payment;
    }
}
