package com.swedbank.common.application.exception;

import lombok.Getter;

@Getter
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
