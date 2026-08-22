package com.commercex.service;

import com.commercex.event.kafka.CommerceEvent;

public interface KafkaEventProducer {

    void publishOrderEvent(String key, Object eventData);

    void publishUserEvent(String key, Object eventData);

    void publishNotificationEvent(String key, Object eventData);

    <T> void publishCustomEvent(String topic, String key, CommerceEvent<T> event);
}
