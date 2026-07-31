package com.commercex.service;

import com.commercex.dto.*;

import java.util.List;

public interface PaymentService {
    PaymentResponse initiatePayment(CreatePaymentRequest request);
    PaymentResponse verifyPayment(String paymentId);
    PaymentResponse cancelPayment(String paymentId);
    PaymentResponse refundPayment(String paymentId, RefundRequest request);
    PaymentResponse getPayment(String paymentId);
    List<PaymentHistoryResponse> getMyPaymentHistory();
    List<PaymentHistoryResponse> getAllPayments();
}
