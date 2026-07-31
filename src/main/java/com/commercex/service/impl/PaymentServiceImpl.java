package com.commercex.service.impl;

import com.commercex.dto.*;
import com.commercex.entity.Order;
import com.commercex.entity.Payment;
import com.commercex.entity.User;
import com.commercex.entity.enums.OrderStatus;
import com.commercex.entity.enums.PaymentStatus;
import com.commercex.exception.*;
import com.commercex.mapper.PaymentMapper;
import com.commercex.repository.OrderRepository;
import com.commercex.repository.PaymentRepository;
import com.commercex.service.PaymentService;
import com.commercex.service.UserService;
import com.commercex.service.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(CreatePaymentRequest request) {
        log.info("Initiating payment for order: {}", request.getOrderNumber());
        User currentUser = userService.getCurrentUser();

        Order order = orderRepository.findByOrderNumber(request.getOrderNumber())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.getOrderNumber()));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new PaymentFailedException("You are not authorized to pay for this order");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new PaymentFailedException("Cannot pay for a cancelled order");
        }

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS || order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new DuplicatePaymentException("Order is already paid");
        }

        // Check if there is an existing pending payment
        List<Payment> existingPayments = paymentRepository.findByOrderId(order.getId());
        Optional<Payment> successfulPayment = existingPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .findFirst();

        if (successfulPayment.isPresent()) {
            throw new DuplicatePaymentException("A successful payment already exists for this order");
        }

        Optional<Payment> pendingPayment = existingPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .findFirst();

        if (pendingPayment.isPresent()) {
            return paymentMapper.toDto(pendingPayment.get());
        }

        Payment payment = Payment.builder()
                .paymentId(generatePaymentId())
                .order(order)
                .amount(order.getTotalAmount())
                .currency("USD")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.CREATED)
                .gatewayName("DummyGateway")
                .build();

        Payment processedPayment = paymentGateway.createPayment(payment);
        
        syncOrderStatus(order, processedPayment);

        return paymentMapper.toDto(paymentRepository.save(processedPayment));
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(String paymentId) {
        log.info("Verifying payment: {}", paymentId);
        Payment payment = getPaymentEntity(paymentId);
        
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS || payment.getPaymentStatus() == PaymentStatus.FAILED) {
            throw new PaymentAlreadyCompletedException("Payment is already finalized");
        }

        Payment verifiedPayment = paymentGateway.verifyPayment(payment);
        
        syncOrderStatus(payment.getOrder(), verifiedPayment);
        
        return paymentMapper.toDto(paymentRepository.save(verifiedPayment));
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(String paymentId) {
        log.info("Cancelling payment: {}", paymentId);
        Payment payment = getPaymentEntity(paymentId);

        User currentUser = userService.getCurrentUser();
        if (!payment.getOrder().getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new PaymentNotFoundException("Payment not found or access denied");
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyCompletedException("Cannot cancel a successful payment. Please request a refund.");
        }

        Payment cancelledPayment = paymentGateway.cancelPayment(payment);
        
        syncOrderStatus(payment.getOrder(), cancelledPayment);
        
        return paymentMapper.toDto(paymentRepository.save(cancelledPayment));
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(String paymentId, RefundRequest request) {
        log.info("Refunding payment: {}", paymentId);
        Payment payment = getPaymentEntity(paymentId);

        User currentUser = userService.getCurrentUser();
        if (!payment.getOrder().getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new PaymentNotFoundException("Payment not found or access denied");
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS && payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RefundNotAllowedException("Only successful payments can be refunded");
        }

        Payment refundedPayment = paymentGateway.refundPayment(payment, request.getReason());
        
        syncOrderStatus(payment.getOrder(), refundedPayment);
        
        return paymentMapper.toDto(paymentRepository.save(refundedPayment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = getPaymentEntity(paymentId);
        
        User currentUser = userService.getCurrentUser();
        if (!payment.getOrder().getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new PaymentNotFoundException("Payment not found or access denied");
        }
        
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getMyPaymentHistory() {
        User currentUser = userService.getCurrentUser();
        List<Order> orders = orderRepository.findByUserId(currentUser.getId());
        
        return orders.stream()
                .flatMap(order -> paymentRepository.findByOrderId(order.getId()).stream())
                .map(paymentMapper::toHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toHistoryDto)
                .collect(Collectors.toList());
    }

    private Payment getPaymentEntity(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
    }

    private void syncOrderStatus(Order order, Payment payment) {
        order.setPaymentStatus(payment.getPaymentStatus());
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS || payment.getPaymentStatus() == PaymentStatus.PAID) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            order.setOrderStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
    }

    private String generatePaymentId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + dateStr + "-" + randomStr;
    }
}
