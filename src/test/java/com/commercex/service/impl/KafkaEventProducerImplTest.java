package com.commercex.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducerImpl kafkaEventProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaEventProducer, "orderEventsTopic", "commercex.events.orders");
        ReflectionTestUtils.setField(kafkaEventProducer, "userEventsTopic", "commercex.events.users");
        ReflectionTestUtils.setField(kafkaEventProducer, "notificationEventsTopic", "commercex.events.notifications");
    }

    @Test
    void publishOrderEvent_DispatchesMessage() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("commercex.events.orders"), eq("ORD-123"), any())).thenReturn(future);

        kafkaEventProducer.publishOrderEvent("ORD-123", Map.of("orderId", "123", "amount", 99.99));

        verify(kafkaTemplate).send(eq("commercex.events.orders"), eq("ORD-123"), any());
    }

    @Test
    void publishUserEvent_DispatchesMessage() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("commercex.events.users"), eq("USR-456"), any())).thenReturn(future);

        kafkaEventProducer.publishUserEvent("USR-456", Map.of("email", "test@commercex.com"));

        verify(kafkaTemplate).send(eq("commercex.events.users"), eq("USR-456"), any());
    }

    @Test
    void publishNotificationEvent_DispatchesMessage() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("commercex.events.notifications"), eq("NOTIF-789"), any())).thenReturn(future);

        kafkaEventProducer.publishNotificationEvent("NOTIF-789", Map.of("message", "Welcome!"));

        verify(kafkaTemplate).send(eq("commercex.events.notifications"), eq("NOTIF-789"), any());
    }
}
