package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewAddedEvent {
    private UUID productId;
    private String userEmail;
}
