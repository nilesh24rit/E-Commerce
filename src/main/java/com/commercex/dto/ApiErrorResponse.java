package com.commercex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Structured error response returned by GlobalExceptionHandler.
 * Contains all fields required for production debugging without exposing stack traces.
 */
@Data
@Builder
public class ApiErrorResponse {

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String error;
    private String message;
    private String path;
    private String correlationId;

    public static ApiErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .correlationId(correlationId)
                .build();
    }
}
