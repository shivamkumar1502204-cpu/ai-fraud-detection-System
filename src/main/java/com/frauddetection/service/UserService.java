package com.frauddetection.service;

import com.frauddetection.dao.UserDAO;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.model.User;
import com.frauddetection.util.AuditLogger;
import com.frauddetection.util.PasswordUtil;
import com.frauddetection.util.ValidationUtil;
import java.util.Optional;

/**
 * Service orchestrating user security, credential hashing, and authentication workflows.
 */
public class UserService {

    private final UserDAO userDAO;
    private final AuditLogger auditLogger;

    public UserService() {
        this.userDAO = new UserDAO();
        this.auditLogger = AuditLogger.getInstance();
    }

    /**
     * Authenticates a user given their credentials.
     *
     * @param username Login identifier
     * @param password Plain-text password candidate
     * @return User object on successful authentication
     * @throws InvalidInputException if credentials are empty or format is invalid
     * @throws DatabaseOperationException on database failure
     */
    public User authenticate(String username, String password) throws InvalidInputException, DatabaseOperationException {
        ValidationUtil.validateNotEmpty(username, "Username");
        ValidationUtil.validateNotEmpty(password, "Password");

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            auditLogger.log(username, "AUTH_FAILED", "Failed authentication attempt: User not found");
            throw new InvalidInputException("Authentication", "Invalid username or password.");
        }

        User user = userOpt.get();
        boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            auditLogger.log(username, "AUTH_FAILED", "Failed authentication attempt: Password mismatch");
            throw new InvalidInputException("Authentication", "Invalid username or password.");
        }

        auditLogger.log(username, "AUTH_SUCCESS", "User authenticated successfully with role: " + user.getRole());
        return user;
    }

    /**
     * Registers a new operator or administrator account.
     *
     * @param username Desired username
     * @param plainPassword Plain-text password
     * @param role "ADMIN" or "USER"
     * @return Created User entity
     */
    public User register(String username, String plainPassword, String role) throws InvalidInputException, DatabaseOperationException {
        ValidationUtil.validateNotEmpty(username, "Username");
        ValidationUtil.validateNotEmpty(plainPassword, "Password");

        if (plainPassword.length() < 6) {
            throw new InvalidInputException("Password", "Password must be at least 6 characters long.");
        }

        Optional<User> existing = userDAO.findByUsername(username.trim());
        if (existing.isPresent()) {
            throw new InvalidInputException("Username", "Username '" + username + "' is already registered.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        User newUser = new User(username.trim(), hash, salt, role);
        newUser.validate();

        User savedUser = userDAO.save(newUser);
        auditLogger.log("SYSTEM", "USER_REGISTERED", "New user registered: " + savedUser.getUsername() + " (" + savedUser.getRole() + ")");
        return savedUser;
    }
}
