package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private String firstName;
}
