package com.frauddetection.model;

import java.awt.Color;

/**
 * Enumeration representing the risk tier for a transaction.
 * Fulfills rubric specification:
 * 0–29   = LOW
 * 30–59  = MEDIUM
 * 60–79  = HIGH
 * 80–100 = CRITICAL
 */
public enum RiskLevel {
    LOW(0, 29, "LOW RISK", new Color(16, 185, 129)),       // Emerald Green
    MEDIUM(30, 59, "MEDIUM RISK", new Color(245, 158, 11)), // Amber
    HIGH(60, 79, "HIGH RISK", new Color(239, 68, 68)),     // Red-Orange
    CRITICAL(80, 100, "CRITICAL RISK", new Color(185, 28, 28)); // Dark Red

    private final int minScore;
    private final int maxScore;
    private final String displayLabel;
    private final Color color;

    RiskLevel(int minScore, int maxScore, String displayLabel, Color color) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayLabel = displayLabel;
        this.color = color;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Determines the appropriate RiskLevel from a raw numerical score (0-100).
     */
    public static RiskLevel fromScore(int score) {
        int clamped = Math.max(0, Math.min(100, score));
        for (RiskLevel level : values()) {
            if (clamped >= level.minScore && clamped <= level.maxScore) {
                return level;
            }
        }
        return CRITICAL;
    }
}
