package com.commercex.service;

import com.commercex.event.kafka.CommerceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {

    @KafkaListener(
            topics = {
                    "${app.kafka.topics.orders:commercex.events.orders}",
                    "${app.kafka.topics.users:commercex.events.users}",
                    "${app.kafka.topics.notifications:commercex.events.notifications}"
            },
            groupId = "${spring.kafka.consumer.group-id:commercex-group}"
    )
    public void consumeEvent(ConsumerRecord<String, Object> record) {
        log.info("Received Kafka message from topic: {} partition: {} offset: {} with key: {}",
                record.topic(), record.partition(), record.offset(), record.key());
        log.debug("Event record value: {}", record.value());

        try {
            Object value = record.value();
            if (value instanceof CommerceEvent<?> commerceEvent) {
                log.info("Processing CommerceEvent [{}] of type: {}", commerceEvent.getEventId(), commerceEvent.getEventType());
                log.trace("CommerceEvent body: {}", commerceEvent.getPayload());
                processCommerceEvent(commerceEvent);
            } else {
                log.debug("Processing generic Kafka payload: {}", value);
            }
        } catch (Exception ex) {
            log.error("Error processing consumed Kafka event from topic: {} key: {}", record.topic(), record.key(), ex);
        }
    }

    private void processCommerceEvent(CommerceEvent<?> event) {
        switch (event.getEventType()) {
            case "ORDER_EVENT" -> log.info("Dispatched OrderEvent for background fulfillment and notifications");
            case "USER_EVENT" -> log.info("Dispatched UserEvent for welcoming and onboarding pipelines");
            case "NOTIFICATION_EVENT" -> log.info("Dispatched NotificationEvent for customer messaging");
            default -> log.warn("Unrecognized Kafka event type: {}", event.getEventType());
        }
    }
}
