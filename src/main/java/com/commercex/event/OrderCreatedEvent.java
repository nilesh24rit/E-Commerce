package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private String userEmail;
}
