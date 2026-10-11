package com.frauddetection.exception;

/**
 * Base checked exception for the AI Powered Fraud Detection System.
 * Serves as the root for all domain-specific application exceptions.
 */
public class FraudSystemException extends Exception {

    public FraudSystemException(String message) {
        super(message);
    }

    public FraudSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
