package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetEvent {
    private String email;
    private String resetToken;
}
