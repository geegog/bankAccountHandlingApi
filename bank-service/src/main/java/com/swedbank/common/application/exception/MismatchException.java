package com.swedbank.common.application.exception;

import lombok.Getter;

@Getter
public class MismatchException extends RuntimeException {

    public MismatchException(String message) {
        super(message);
    }
}
