package com.commercex.service.impl;

import com.commercex.dto.CreatePaymentRequest;
import com.commercex.dto.PaymentResponse;
import com.commercex.entity.Order;
import com.commercex.entity.Payment;
import com.commercex.entity.User;
import com.commercex.entity.enums.OrderStatus;
import com.commercex.entity.enums.PaymentMethod;
import com.commercex.entity.enums.PaymentStatus;
import com.commercex.exception.DuplicatePaymentException;
import com.commercex.mapper.PaymentMapper;
import com.commercex.repository.OrderRepository;
import com.commercex.repository.PaymentRepository;
import com.commercex.service.UserService;
import com.commercex.service.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderNumber("ORD-123");
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
    }

    @Test
    void initiatePayment_Success() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("ORD-123");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Collections.emptyList());

        Payment gatewayResponse = new Payment();
        gatewayResponse.setPaymentStatus(PaymentStatus.SUCCESS);
        when(paymentGateway.createPayment(any(Payment.class))).thenReturn(gatewayResponse);
        
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        
        PaymentResponse responseDto = PaymentResponse.builder().paymentStatus(PaymentStatus.SUCCESS).build();
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(responseDto);

        PaymentResponse result = paymentService.initiatePayment(request);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
    }

    @Test
    void initiatePayment_DuplicatePayment_ThrowsException() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNumber("ORD-123");
        
        order.setPaymentStatus(PaymentStatus.SUCCESS);

        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));

        assertThrows(DuplicatePaymentException.class, () -> paymentService.initiatePayment(request));
    }
}
