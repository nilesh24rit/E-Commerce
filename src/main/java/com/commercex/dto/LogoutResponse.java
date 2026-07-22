package com.commercex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LogoutResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
