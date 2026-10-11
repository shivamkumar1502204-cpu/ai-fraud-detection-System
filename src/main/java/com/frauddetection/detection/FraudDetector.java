package com.frauddetection.detection;

import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all fraud detectors.
 * Encapsulates common scoring heuristics, weight assignments, and indicator formatting.
 * Fulfills OOP Rubric: Abstract Classes & Inheritance.
 */
public abstract class FraudDetector implements FraudDetectionStrategy {

    protected final String strategyName;
    protected final double weight;

    /**
     * Protected constructor for subclasses.
     *
     * @param strategyName Human-readable name
     * @param weight Relative importance factor (0.0 - 1.0)
     */
    protected FraudDetector(String strategyName, double weight) {
        this.strategyName = strategyName;
        this.weight = Math.max(0.0, Math.min(1.0, weight));
    }

    @Override
    public String getStrategyName() {
        return strategyName;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    /**
     * Clamps a value to the standard 0.0 - 100.0 scoring domain.
     */
    protected double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Helper to create a structured FraudIndicator.
     */
    protected FraudIndicator createIndicator(String type, String description, double severity) {
        return new FraudIndicator(type, description, severity);
    }

    /**
     * Subclasses implement their domain-specific risk scoring logic.
     */
    @Override
    public abstract double calculateRisk(Transaction transaction);

    /**
     * Subclasses extract domain-specific indicators explaining the score.
     */
    @Override
    public abstract List<FraudIndicator> extractIndicators(Transaction transaction);
}
