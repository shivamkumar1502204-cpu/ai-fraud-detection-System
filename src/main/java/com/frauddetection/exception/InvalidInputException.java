package com.frauddetection.exception;

/**
 * Thrown when user inputs fail structural or semantic validation constraints
 * (e.g., negative amounts, blank required fields, malformed URLs).
 */
public class InvalidInputException extends FraudSystemException {

    private final String fieldName;

    public InvalidInputException(String message) {
        super(message);
        this.fieldName = "Unknown";
    }

    public InvalidInputException(String fieldName, String message) {
        super(String.format("Validation error on '%s': %s", fieldName, message));
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
