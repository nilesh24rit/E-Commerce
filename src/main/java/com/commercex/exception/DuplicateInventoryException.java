package com.commercex.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateInventoryException extends RuntimeException {
    public DuplicateInventoryException(String message) {
        super(message);
    }
}
