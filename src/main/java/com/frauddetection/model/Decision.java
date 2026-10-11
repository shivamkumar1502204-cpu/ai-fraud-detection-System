package com.frauddetection.model;

import java.awt.Color;

/**
 * Enumeration representing the final operational fraud determination.
 * Fulfills rubric specification: SAFE / SUSPICIOUS / FRAUDULENT
 */
public enum Decision {
    SAFE("SAFE", "Transaction deemed safe. Proceed with standard processing.", new Color(16, 185, 129)),
    SUSPICIOUS("SUSPICIOUS", "Transaction flagged. Secondary verification/MFA recommended.", new Color(245, 158, 11)),
    FRAUDULENT("FRAUDULENT", "High probability of fraud. Immediate intervention recommended.", new Color(220, 38, 38));

    private final String label;
    private final String description;
    private final Color color;

    Decision(String label, String description, Color color) {
        this.label = label;
        this.description = description;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Maps an AI risk score to a triage decision:
     * - Score < 30 => SAFE
     * - Score 30..69 => SUSPICIOUS
     * - Score >= 70 => FRAUDULENT
     */
    public static Decision fromScore(int score) {
        if (score < 30) {
            return SAFE;
        } else if (score < 70) {
            return SUSPICIOUS;
        } else {
            return FRAUDULENT;
        }
    }
}
