package com.frauddetection.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain entity representing the synthesized result of the AI Fraud Detection Engine.
 * Fulfills Collections requirement by storing multiple reasons and indicators in generic Lists.
 */
public class FraudAnalysis {

    private int id;
    private int transactionId;
    private String transactionCode; // Business transaction identifier for display
    private int riskScore; // 0 - 100
    private RiskLevel riskLevel;
    private Decision decision;
    private String recommendation;
    private final List<String> reasons;
    private final List<FraudIndicator> indicators;
    private LocalDateTime analysisTime;

    public FraudAnalysis() {
        this.reasons = new ArrayList<>();
        this.indicators = new ArrayList<>();
        this.analysisTime = LocalDateTime.now();
        this.riskScore = 0;
        this.riskLevel = RiskLevel.LOW;
        this.decision = Decision.SAFE;
    }

    public FraudAnalysis(int id, int transactionId, String transactionCode, int riskScore,
                         RiskLevel riskLevel, Decision decision, String recommendation,
                         List<String> reasons, List<FraudIndicator> indicators, LocalDateTime analysisTime) {
        this.id = id;
        this.transactionId = transactionId;
        this.transactionCode = transactionCode;
        this.riskScore = Math.max(0, Math.min(100, riskScore));
        this.riskLevel = riskLevel != null ? riskLevel : RiskLevel.fromScore(riskScore);
        this.decision = decision != null ? decision : Decision.fromScore(riskScore);
        this.recommendation = recommendation;
        this.reasons = reasons != null ? new ArrayList<>(reasons) : new ArrayList<>();
        this.indicators = indicators != null ? new ArrayList<>(indicators) : new ArrayList<>();
        this.analysisTime = analysisTime != null ? analysisTime : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = Math.max(0, Math.min(100, riskScore));
        this.riskLevel = RiskLevel.fromScore(this.riskScore);
        this.decision = Decision.fromScore(this.riskScore);
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }

    public void addReason(String reason) {
        if (reason != null && !reason.trim().isEmpty()) {
            this.reasons.add(reason.trim());
        }
    }

    public void setReasons(List<String> newReasons) {
        this.reasons.clear();
        if (newReasons != null) {
            this.reasons.addAll(newReasons);
        }
    }

    public List<FraudIndicator> getIndicators() {
        return Collections.unmodifiableList(indicators);
    }

    public void addIndicator(FraudIndicator indicator) {
        if (indicator != null) {
            this.indicators.add(indicator);
        }
    }

    public void setIndicators(List<FraudIndicator> newIndicators) {
        this.indicators.clear();
        if (newIndicators != null) {
            this.indicators.addAll(newIndicators);
        }
    }

    public LocalDateTime getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(LocalDateTime analysisTime) {
        this.analysisTime = analysisTime;
    }

    @Override
    public String toString() {
        return "FraudAnalysis{" +
                "id=" + id +
                ", transactionCode='" + transactionCode + '\'' +
                ", riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", decision=" + decision +
                ", indicatorsCount=" + indicators.size() +
                '}';
    }
}
