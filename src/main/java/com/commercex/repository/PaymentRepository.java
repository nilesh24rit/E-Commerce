package com.commercex.repository;

import com.commercex.entity.Payment;
import com.commercex.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentId(String paymentId);
    List<Payment> findByOrderId(UUID orderId);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
