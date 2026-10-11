package com.frauddetection.util;

import com.frauddetection.exception.InvalidInputException;
import java.net.URI;

/**
 * Utility class providing centralized validation routines and generic sanity checks.
 * Fulfills OOP & Generics requirements.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Generic null-check utility ensuring safe usage of reference types.
     * Demonstrates Generics usage.
     *
     * @param <T> Object type
     * @param object Target object
     * @param fieldName Name of the field for error reporting
     * @return The non-null object
     * @throws InvalidInputException if object is null
     */
    public static <T> T requireNonNull(T object, String fieldName) throws InvalidInputException {
        if (object == null) {
            throw new InvalidInputException(fieldName, "Value cannot be null");
        }
        return object;
    }

    /**
     * Validates that a string is neither null nor purely whitespace.
     */
    public static void validateNotEmpty(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName, "Cannot be empty or blank");
        }
    }

    /**
     * Validates that a monetary amount is positive and strictly greater than zero.
     */
    public static void validatePositive(double amount, String fieldName) throws InvalidInputException {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0.0) {
            throw new InvalidInputException(fieldName, "Amount must be a positive number greater than 0.00");
        }
    }

    /**
     * Validates that an integer count/frequency is non-negative.
     */
    public static void validateNonNegative(int value, String fieldName) throws InvalidInputException {
        if (value < 0) {
            throw new InvalidInputException(fieldName, "Value cannot be negative");
        }
    }

    /**
     * Validates URL structure if provided. URLs are optional, but if present must be structurally valid.
     */
    public static void validateUrlIfPresent(String urlString) throws InvalidInputException {
        if (urlString == null || urlString.trim().isEmpty()) {
            return;
        }
        try {
            java.net.URI uri = java.net.URI.create(urlString.trim());
            if (uri.getScheme() == null) {
                throw new InvalidInputException("URL", "Missing URL scheme (e.g., http:// or https://): " + urlString);
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("URL", "Malformed URL syntax: " + urlString);
        }
    }

    /**
     * Enforces maximum string length constraints.
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) throws InvalidInputException {
        if (value != null && value.length() > maxLength) {
            throw new InvalidInputException(fieldName,
                    String.format("Exceeds maximum allowed length of %d characters (length was %d)", maxLength, value.length()));
        }
    }
}
