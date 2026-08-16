package com.commercex.event;

import com.commercex.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationEventTest {

    @Mock
    private EmailService emailService;

    @Test
    void testUserRegisteredEvent_CreationAndListening() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(userId, "user@test.com", "Alice");

        assertEquals(userId, event.getUserId());
        assertEquals("user@test.com", event.getEmail());
        assertEquals("Alice", event.getFirstName());

        emailService.handleUserRegistered(event);
        verify(emailService).handleUserRegistered(event);
    }

    @Test
    void testOrderCancelledEvent_CreationAndListening() {
        UUID orderId = UUID.randomUUID();
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, "user@test.com", "Changed mind");

        assertEquals(orderId, event.getOrderId());
        assertEquals("user@test.com", event.getUserEmail());
        assertEquals("Changed mind", event.getReason());

        emailService.handleOrderCancelled(event);
        verify(emailService).handleOrderCancelled(event);
    }

    @Test
    void testRefundProcessedEvent_CreationAndListening() {
        UUID paymentId = UUID.randomUUID();
        RefundProcessedEvent event = new RefundProcessedEvent(paymentId, "user@test.com", BigDecimal.valueOf(99.99), "Return");

        assertEquals(paymentId, event.getPaymentId());
        assertEquals("user@test.com", event.getUserEmail());
        assertEquals(BigDecimal.valueOf(99.99), event.getAmount());
        assertEquals("Return", event.getReason());

        emailService.handleRefundProcessed(event);
        verify(emailService).handleRefundProcessed(event);
    }

    @Test
    void testPasswordResetEvent_CreationAndListening() {
        PasswordResetEvent event = new PasswordResetEvent("user@test.com", "reset-token-123");

        assertEquals("user@test.com", event.getEmail());
        assertEquals("reset-token-123", event.getResetToken());

        emailService.handlePasswordReset(event);
        verify(emailService).handlePasswordReset(event);
    }
}
