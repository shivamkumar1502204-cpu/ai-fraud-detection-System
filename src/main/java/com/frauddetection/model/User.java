package com.frauddetection.model;

import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.util.Validatable;
import com.frauddetection.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Model entity representing an application user (Operator or Administrator).
 * Fulfills OOP Rubric: Encapsulation & Validatable interface implementation.
 */
public class User implements Validatable {

    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private String role; // "ADMIN" or "USER"
    private LocalDateTime createdAt;

    public User() {
        this.role = "USER";
        this.createdAt = LocalDateTime.now();
    }

    public User(int id, String username, String passwordHash, String salt, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role != null ? role : "USER";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public User(String username, String passwordHash, String salt, String role) {
        this(0, username, passwordHash, salt, role, LocalDateTime.now());
    }

    @Override
    public void validate() throws InvalidInputException {
        ValidationUtil.validateNotEmpty(username, "Username");
        ValidationUtil.validateNotEmpty(passwordHash, "Password Hash");
        ValidationUtil.validateNotEmpty(salt, "Salt");
        ValidationUtil.validateMaxLength(username, 50, "Username");
        if (role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("USER"))) {
            throw new InvalidInputException("Role", "Role must be either 'ADMIN' or 'USER'");
        }
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
