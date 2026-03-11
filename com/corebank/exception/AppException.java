package com.corebank.exception;

/**
 * Base exception for CoreBank.
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
