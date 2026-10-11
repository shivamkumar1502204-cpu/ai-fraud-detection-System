package com.frauddetection.exception;

/**
 * Thrown when a transaction entity is structurally invalid or violates business rules.
 */
public class InvalidTransactionException extends FraudSystemException {

    private final String transactionId;

    public InvalidTransactionException(String message) {
        super(message);
        this.transactionId = "N/A";
    }

    public InvalidTransactionException(String transactionId, String message) {
        super(String.format("Invalid transaction [%s]: %s", transactionId, message));
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
