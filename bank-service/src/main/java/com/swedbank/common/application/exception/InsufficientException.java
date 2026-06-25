package com.swedbank.common.application.exception;

import lombok.Getter;

@Getter
public class InsufficientException extends RuntimeException {

    public InsufficientException(String message) {
        super(message);
    }
}
