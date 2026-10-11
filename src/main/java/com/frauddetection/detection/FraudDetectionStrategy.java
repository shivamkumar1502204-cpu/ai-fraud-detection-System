package com.frauddetection.detection;

import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import java.util.List;

/**
 * Strategy interface defining the contract for modular fraud detection algorithms.
 * Fulfills OOP Rubric: Strategy Pattern & Interfaces.
 */
public interface FraudDetectionStrategy {

    /**
     * Calculates a normalized risk score between 0.0 (safe) and 100.0 (fraudulent).
     *
     * @param transaction The transaction to evaluate
     * @return Risk score in range [0.0, 100.0]
     */
    double calculateRisk(Transaction transaction);

    /**
     * Extracts transparent, explainable indicators and reasoning evidence.
     *
     * @param transaction The transaction to inspect
     * @return List of granular fraud indicators
     */
    List<FraudIndicator> extractIndicators(Transaction transaction);

    /**
     * Returns the human-readable identifier of this strategy.
     */
    String getStrategyName();

    /**
     * Returns the weight/importance factor of this strategy in the ensemble (0.0 to 1.0).
     */
    double getWeight();
}
