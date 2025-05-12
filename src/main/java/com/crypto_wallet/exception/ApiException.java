package com.crypto_wallet.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception class for handling API errors.
 */
public class ApiException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
