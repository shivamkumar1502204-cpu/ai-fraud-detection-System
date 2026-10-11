package com.frauddetection.detection;

import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates transactional metrics: monetary magnitude, sudden spikes relative to baseline,
 * high-risk payment categories, and velocity frequency.
 * Fulfills OOP Rubric: Inheritance (extends FraudDetector).
 */
public class TransactionFraudDetector extends FraudDetector {

    public TransactionFraudDetector() {
        super("Transaction Heuristics Detector", 0.35); // 35% ensemble weight
    }

    @Override
    public double calculateRisk(Transaction transaction) {
        if (transaction == null) {
            return 0.0;
        }

        double riskScore = 0.0;
        double amount = transaction.getAmount();
        double prevAmount = transaction.getPreviousAmount();
        String type = transaction.getTransactionType() != null ? transaction.getTransactionType().toUpperCase() : "";
        int frequency = transaction.getTransactionFrequency();

        // 1. Absolute Amount Threshold Scoring
        if (amount >= 20000.0) {
            riskScore += 45.0;
        } else if (amount >= 10000.0) {
            riskScore += 30.0;
        } else if (amount >= 5000.0) {
            riskScore += 18.0;
        } else if (amount >= 2000.0) {
            riskScore += 10.0;
        }

        // 2. Relative Baseline Spike Scoring (Deviation from Historical Mean)
        if (prevAmount > 0.0) {
            double ratio = amount / prevAmount;
            if (ratio >= 20.0) {
                riskScore += 40.0;
            } else if (ratio >= 8.0) {
                riskScore += 25.0;
            } else if (ratio >= 4.0) {
                riskScore += 15.0;
            }
        }

        // 3. High-Risk Transaction Modality
        if (type.contains("CRYPTO") || type.contains("BITCOIN")) {
            riskScore += 25.0;
        } else if (type.contains("INTERNATIONAL") || type.contains("WIRE") || type.contains("SWIFT")) {
            riskScore += 20.0;
        } else if (type.contains("CASH_ADVANCE") || type.contains("MONEY_ORDER")) {
            riskScore += 15.0;
        }

        // 4. Transaction Burst Velocity (Transactions within 1 hour)
        if (frequency >= 10) {
            riskScore += 35.0;
        } else if (frequency >= 6) {
            riskScore += 20.0;
        } else if (frequency >= 3) {
            riskScore += 10.0;
        }

        return clampScore(riskScore);
    }

    @Override
    public List<FraudIndicator> extractIndicators(Transaction transaction) {
        List<FraudIndicator> indicators = new ArrayList<>();
        if (transaction == null) {
            return indicators;
        }

        double amount = transaction.getAmount();
        double prevAmount = transaction.getPreviousAmount();
        String type = transaction.getTransactionType() != null ? transaction.getTransactionType() : "STANDARD";
        int frequency = transaction.getTransactionFrequency();

        if (amount >= 10000.0) {
            indicators.add(createIndicator("AMOUNT_LARGE",
                    String.format("Unusually high transaction volume: $%,.2f", amount), 30.0));
        }

        if (prevAmount > 0.0 && (amount / prevAmount) >= 5.0) {
            double ratio = amount / prevAmount;
            indicators.add(createIndicator("AMOUNT_SPIKE",
                    String.format("Transaction amount is %.1fx higher than sender's previous baseline ($%,.2f)", ratio, prevAmount), 25.0));
        }

        if (type.toUpperCase().contains("CRYPTO")) {
            indicators.add(createIndicator("HIGH_RISK_MODALITY",
                    "Cryptocurrency purchase flagged due to high irreversibility risk", 20.0));
        } else if (type.toUpperCase().contains("INTERNATIONAL") || type.toUpperCase().contains("WIRE")) {
            indicators.add(createIndicator("HIGH_RISK_MODALITY",
                    "Cross-border international wire transfer flagged for compliance inspection", 18.0));
        }

        if (frequency >= 5) {
            indicators.add(createIndicator("VELOCITY_SPIKE",
                    String.format("Abnormal velocity burst: %d transactions initiated within the past hour", frequency), 20.0));
        }

        return indicators;
    }
}
