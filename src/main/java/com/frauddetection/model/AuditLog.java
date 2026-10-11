package com.frauddetection.model;

import java.time.LocalDateTime;

/**
 * Model representing an immutable audit log record for regulatory compliance and security tracing.
 */
public class AuditLog {

    private int id;
    private String username;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String username, String action, String details) {
        this(0, username, action, details, LocalDateTime.now());
    }

    public AuditLog(int id, String username, String action, String details, LocalDateTime timestamp) {
        this.id = id;
        this.username = username != null ? username : "ANONYMOUS";
        this.action = action;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", user='" + username + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
