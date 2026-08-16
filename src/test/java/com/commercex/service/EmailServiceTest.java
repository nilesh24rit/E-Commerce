package com.commercex.service;

import com.commercex.event.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendHtmlEmail_Success() {
        emailService.sendHtmlEmail("test@example.com", "Test Subject", "<h1>Test</h1>");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void handleUserRegistered_Success() {
        when(templateEngine.process(eq("email/user-registered"), any(Context.class))).thenReturn("<html>Welcome</html>");
        
        UserRegisteredEvent event = new UserRegisteredEvent(UUID.randomUUID(), "john@example.com", "John");
        emailService.handleUserRegistered(event);

        verify(templateEngine).process(eq("email/user-registered"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void handleOrderCreated_Success() {
        when(templateEngine.process(eq("email/order-created"), any(Context.class))).thenReturn("<html>Order Created</html>");
        
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.randomUUID(), "john@example.com");
        emailService.handleOrderCreated(event);

        verify(templateEngine).process(eq("email/order-created"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void handlePaymentCompleted_Success() {
        when(templateEngine.process(eq("email/payment-completed"), any(Context.class))).thenReturn("<html>Payment Successful</html>");
        
        PaymentCompletedEvent event = new PaymentCompletedEvent(UUID.randomUUID(), "john@example.com", "100.00");
        emailService.handlePaymentCompleted(event);

        verify(templateEngine).process(eq("email/payment-completed"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void handlePasswordReset_Success() {
        when(templateEngine.process(eq("email/password-reset"), any(Context.class))).thenReturn("<html>Password Reset</html>");
        
        PasswordResetEvent event = new PasswordResetEvent("john@example.com", "token123");
        emailService.handlePasswordReset(event);

        verify(templateEngine).process(eq("email/password-reset"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void handleOrderCancelled_Success() {
        when(templateEngine.process(eq("email/order-cancelled"), any(Context.class))).thenReturn("<html>Order Cancelled</html>");
        
        OrderCancelledEvent event = new OrderCancelledEvent(UUID.randomUUID(), "john@example.com", "Customer request");
        emailService.handleOrderCancelled(event);

        verify(templateEngine).process(eq("email/order-cancelled"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void handleRefundProcessed_Success() {
        when(templateEngine.process(eq("email/refund-processed"), any(Context.class))).thenReturn("<html>Refund Processed</html>");
        
        RefundProcessedEvent event = new RefundProcessedEvent(UUID.randomUUID(), "john@example.com", BigDecimal.valueOf(50.00), "Defective product");
        emailService.handleRefundProcessed(event);

        verify(templateEngine).process(eq("email/refund-processed"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }
}
