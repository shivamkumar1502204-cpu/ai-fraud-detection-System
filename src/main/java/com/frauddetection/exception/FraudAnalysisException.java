package com.frauddetection.exception;

/**
 * Thrown when an error occurs during the AI hybrid risk scoring pipeline.
 */
public class FraudAnalysisException extends FraudSystemException {

    public FraudAnalysisException(String message) {
        super(message);
    }

    public FraudAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
