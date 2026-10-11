package com.frauddetection.model;

/**
 * Model representing an explainable indicator or risk feature identified by the detection engine.
 * Provides granular transparency into why a score was generated.
 */
public class FraudIndicator {

    private int id;
    private int analysisId;
    private String indicatorType;
    private String indicatorDescription;
    private double severityWeight;

    public FraudIndicator() {
    }

    public FraudIndicator(String indicatorType, String indicatorDescription, double severityWeight) {
        this(0, 0, indicatorType, indicatorDescription, severityWeight);
    }

    public FraudIndicator(int id, int analysisId, String indicatorType, String indicatorDescription, double severityWeight) {
        this.id = id;
        this.analysisId = analysisId;
        this.indicatorType = indicatorType;
        this.indicatorDescription = indicatorDescription;
        this.severityWeight = severityWeight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(int analysisId) {
        this.analysisId = analysisId;
    }

    public String getIndicatorType() {
        return indicatorType;
    }

    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    public String getIndicatorDescription() {
        return indicatorDescription;
    }

    public void setIndicatorDescription(String indicatorDescription) {
        this.indicatorDescription = indicatorDescription;
    }

    public double getSeverityWeight() {
        return severityWeight;
    }

    public void setSeverityWeight(double severityWeight) {
        this.severityWeight = severityWeight;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Weight: +%.1f)", indicatorType, indicatorDescription, severityWeight);
    }
}
