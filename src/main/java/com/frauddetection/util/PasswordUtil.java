package com.frauddetection.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class providing secure password hashing using SHA-256 with per-user salt.
 * Academic Note: In enterprise banking production, adaptive key-derivation functions
 * such as Argon2id or BCrypt are recommended. SHA-256 + Salt is implemented here
 * using native standard Java SE libraries for zero-dependency presentation readiness.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Generates a cryptographically secure 16-byte random salt encoded as hexadecimal.
     *
     * @return Hex-encoded salt string
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Hashes a plain-text password combined with a salt using SHA-256.
     *
     * @param password Plain-text password
     * @param salt Hex-encoded salt
     * @return Hex-encoded 64-character SHA-256 hash
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Salt prefixing prevents rainbow table attacks
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies that a plain-text candidate password matches the stored hash and salt.
     *
     * @param candidatePassword Plain-text password to test
     * @param salt Stored hex salt
     * @param expectedHash Stored hex hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String candidatePassword, String salt, String expectedHash) {
        if (candidatePassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(candidatePassword, salt);
        return MessageDigest.isEqual(
                calculatedHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
