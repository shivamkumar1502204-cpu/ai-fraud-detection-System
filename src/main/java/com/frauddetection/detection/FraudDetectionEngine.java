package com.frauddetection.detection;

import com.frauddetection.exception.FraudAnalysisException;
import com.frauddetection.model.Decision;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.RiskLevel;
import com.frauddetection.model.Transaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hybrid AI-Inspired Fraud Risk Detection Engine.
 * Coordinates an ensemble of specialized detectors polymorphically to synthesize
 * a holistic, explainable fraud risk score and triage recommendation.
 * Fulfills OOP Rubric: Polymorphism, Encapsulation, Collections & Generics.
 */
public class FraudDetectionEngine {

    // Polymorphic storage: Parent reference holding child detector instances
    private final List<FraudDetector> detectors;
    private final Map<String, Double> strategyWeights;

    public FraudDetectionEngine() {
        this.detectors = new ArrayList<>();
        this.strategyWeights = new HashMap<>();

        // Register detector strategies in ensemble
        registerDetector(new TransactionFraudDetector());
        registerDetector(new TextFraudDetector());
        registerDetector(new URLFraudDetector());
        registerDetector(new AnomalyFraudDetector());
    }

    /**
     * Polymorphic registration of detectors.
     */
    public void registerDetector(FraudDetector detector) {
        if (detector != null) {
            this.detectors.add(detector);
            this.strategyWeights.put(detector.getStrategyName(), detector.getWeight());
        }
    }

    /**
     * Executes the hybrid AI risk scoring pipeline against a transaction.
     * Demonstrates Polymorphism by executing common methods through parent references.
     *
     * @param transaction Transaction telemetry payload
     * @return Complete, explainable FraudAnalysis result
     * @throws FraudAnalysisException if evaluation fails
     */
    public FraudAnalysis analyze(Transaction transaction) throws FraudAnalysisException {
        if (transaction == null) {
            throw new FraudAnalysisException("Cannot execute fraud analysis on null transaction");
        }

        try {
            double totalWeightedScore = 0.0;
            double sumOfWeights = 0.0;
            List<FraudIndicator> aggregatedIndicators = new ArrayList<>();
            List<String> explainableReasons = new ArrayList<>();
            double maxRawThreat = 0.0;

            // Polymorphic iteration over detector strategies
            for (FraudDetector detector : detectors) {
                double rawScore = detector.calculateRisk(transaction);
                double weight = detector.getWeight();

                if (rawScore > maxRawThreat) {
                    maxRawThreat = rawScore;
                }

                totalWeightedScore += (rawScore * weight);
                sumOfWeights += weight;

                // Extract explainable evidence
                List<FraudIndicator> indicators = detector.extractIndicators(transaction);
                if (indicators != null && !indicators.isEmpty()) {
                    aggregatedIndicators.addAll(indicators);
                    for (FraudIndicator ind : indicators) {
                        explainableReasons.add(ind.getIndicatorDescription());
                    }
                }
            }

            // Calculate normalized composite score with Ensemble Threat Fusion:
            // Standard weighted average works well when multiple signals co-occur,
            // but peak pooling ensures acute threats (e.g. OTP harvesting, severe money anomalies)
            // are not unsafely diluted by irrelevant features.
            double weightedAverage = sumOfWeights > 0 ? (totalWeightedScore / sumOfWeights) : 0.0;
            double fusedScore;
            if (maxRawThreat >= 70.0) {
                // Severe single vector threat (e.g., OTP theft, extreme baseline spike)
                fusedScore = (0.70 * maxRawThreat) + (0.30 * weightedAverage);
            } else if (maxRawThreat >= 40.0) {
                // Elevated single vector threat
                fusedScore = (0.50 * maxRawThreat) + (0.50 * weightedAverage);
            } else {
                fusedScore = weightedAverage;
            }

            int compositeScore = (int) Math.round(fusedScore);
            compositeScore = Math.max(0, Math.min(100, compositeScore));

            // Determine Risk Level and Operational Decision
            RiskLevel riskLevel = RiskLevel.fromScore(compositeScore);
            Decision decision = Decision.fromScore(compositeScore);

            // Generate context-aware actionable recommendation
            String recommendation = generateRecommendation(riskLevel, decision, aggregatedIndicators);

            // Provide default benign reasons if no flags were triggered
            if (explainableReasons.isEmpty()) {
                explainableReasons.add("All evaluated telemetry parameters conform to standard baseline profile.");
                explainableReasons.add("No anomalous velocity, credential requests, or hostile URLs identified.");
            }

            // Synthesize and return final FraudAnalysis model
            FraudAnalysis analysis = new FraudAnalysis();
            analysis.setTransactionId(transaction.getId());
            analysis.setTransactionCode(transaction.getTransactionId());
            analysis.setRiskScore(compositeScore);
            analysis.setRiskLevel(riskLevel);
            analysis.setDecision(decision);
            analysis.setRecommendation(recommendation);
            analysis.setReasons(explainableReasons);
            analysis.setIndicators(aggregatedIndicators);
            analysis.setAnalysisTime(LocalDateTime.now());

            return analysis;

        } catch (Exception e) {
            throw new FraudAnalysisException("Error during hybrid fraud evaluation: " + e.getMessage(), e);
        }
    }

    /**
     * Synthesizes actionable, regulatory-compliant recommendations.
     */
    private String generateRecommendation(RiskLevel level, Decision decision, List<FraudIndicator> indicators) {
        switch (level) {
            case LOW:
                return "Approve transaction. Standard telemetry cleared with no significant risk flags.";
            case MEDIUM:
                return "Flag for secondary review. Require Step-Up Multi-Factor Authentication (SMS/Authenticator OTP) before settling.";
            case HIGH:
                return "Hold transaction. Contact cardholder via registered primary phone to verify authorization.";
            case CRITICAL:
            default:
                return "DO NOT PROCEED. High probability of fraud detected. Freeze pending funds and initiate account takeover investigation.";
        }
    }

    public List<FraudDetector> getDetectors() {
        return Collections.unmodifiableList(detectors);
    }

    public Map<String, Double> getStrategyWeights() {
        return Collections.unmodifiableMap(strategyWeights);
    }
}
