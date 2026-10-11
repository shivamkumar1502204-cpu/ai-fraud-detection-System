package com.frauddetection.exception;

/**
 * Thrown when a database/JDBC operation encounters an unrecoverable SQL error,
 * connection failure, or constraint violation.
 */
public class DatabaseOperationException extends FraudSystemException {

    private final String sqlState;
    private final int errorCode;

    public DatabaseOperationException(String message) {
        super(message);
        this.sqlState = "UNKNOWN";
        this.errorCode = 0;
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
        this.sqlState = "UNKNOWN";
        this.errorCode = 0;
    }

    public DatabaseOperationException(String message, String sqlState, int errorCode, Throwable cause) {
        super(String.format("%s (SQLState: %s, ErrorCode: %d)", message, sqlState, errorCode), cause);
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
