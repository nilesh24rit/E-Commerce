package com.commercex.service.impl;

import com.commercex.event.kafka.CommerceEvent;
import com.commercex.service.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducerImpl implements KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.orders:commercex.events.orders}")
    private String orderEventsTopic;

    @Value("${app.kafka.topics.users:commercex.events.users}")
    private String userEventsTopic;

    @Value("${app.kafka.topics.notifications:commercex.events.notifications}")
    private String notificationEventsTopic;

    @Override
    public void publishOrderEvent(String key, Object eventData) {
        CommerceEvent<Object> event = CommerceEvent.builder()
                .eventType("ORDER_EVENT")
                .payload(eventData)
                .build();
        publishCustomEvent(orderEventsTopic, key, event);
    }

    @Override
    public void publishUserEvent(String key, Object eventData) {
        CommerceEvent<Object> event = CommerceEvent.builder()
                .eventType("USER_EVENT")
                .payload(eventData)
                .build();
        publishCustomEvent(userEventsTopic, key, event);
    }

    @Override
    public void publishNotificationEvent(String key, Object eventData) {
        CommerceEvent<Object> event = CommerceEvent.builder()
                .eventType("NOTIFICATION_EVENT")
                .payload(eventData)
                .build();
        publishCustomEvent(notificationEventsTopic, key, event);
    }

    @Override
    public <T> void publishCustomEvent(String topic, String key, CommerceEvent<T> event) {
        log.debug("Publishing Kafka event to topic: {} with key: {} and eventId: {}", topic, key, event.getEventId());
        log.trace("Full event payload: {}", event);

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka event [{}] successfully sent to topic: {} partition: {} offset: {}",
                            event.getEventId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send Kafka event [{}] to topic: {}", event.getEventId(), topic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Synchronous error initiating Kafka dispatch for eventId: {}", event.getEventId(), e);
        }
    }
}
