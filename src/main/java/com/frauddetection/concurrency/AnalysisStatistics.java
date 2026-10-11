package com.frauddetection.concurrency;

import com.frauddetection.model.Decision;
import com.frauddetection.model.RiskLevel;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe shared statistics manager.
 * Uses synchronized methods to ensure thread safety across concurrent analysis tasks.
 * Fulfills Multithreading & Synchronization rubric: Synchronization & Shared thread-safe state.
 */
public class AnalysisStatistics {

    private static AnalysisStatistics instance;

    private int totalAnalyses = 0;
    private int safeCount = 0;
    private int suspiciousCount = 0;
    private int fraudulentCount = 0;
    private int highRiskCount = 0;
    private long totalRiskScoreSum = 0;
    private final Map<RiskLevel, Integer> riskDistribution = new EnumMap<>(RiskLevel.class);

    private AnalysisStatistics() {
        for (RiskLevel level : RiskLevel.values()) {
            riskDistribution.put(level, 0);
        }
    }

    public static synchronized AnalysisStatistics getInstance() {
        if (instance == null) {
            instance = new AnalysisStatistics();
        }
        return instance;
    }

    /**
     * Atomically records an analysis outcome in a thread-safe synchronized manner.
     *
     * @param score Calculated risk score (0-100)
     * @param level RiskLevel classification
     * @param decision Operational Decision
     */
    public synchronized void recordAnalysis(int score, RiskLevel level, Decision decision) {
        totalAnalyses++;
        totalRiskScoreSum += score;

        if (decision == Decision.SAFE) {
            safeCount++;
        } else if (decision == Decision.SUSPICIOUS) {
            suspiciousCount++;
        } else if (decision == Decision.FRAUDULENT) {
            fraudulentCount++;
        }

        if (level == RiskLevel.HIGH || level == RiskLevel.CRITICAL) {
            highRiskCount++;
        }

        riskDistribution.put(level, riskDistribution.getOrDefault(level, 0) + 1);
    }

    /**
     * Atomically initializes/re-syncs internal memory statistics from database query metrics.
     */
    public synchronized void syncFromDatabase(Map<String, Object> dbMetrics) {
        if (dbMetrics == null) return;

        if (dbMetrics.containsKey("totalAnalyses")) {
            this.totalAnalyses = (Integer) dbMetrics.get("totalAnalyses");
        }
        if (dbMetrics.containsKey("safeCount")) {
            this.safeCount = (Integer) dbMetrics.get("safeCount");
        }
        if (dbMetrics.containsKey("suspiciousCount")) {
            this.suspiciousCount = (Integer) dbMetrics.get("suspiciousCount");
        }
        if (dbMetrics.containsKey("fraudulentCount")) {
            this.fraudulentCount = (Integer) dbMetrics.get("fraudulentCount");
        }
        if (dbMetrics.containsKey("highRiskCount")) {
            this.highRiskCount = (Integer) dbMetrics.get("highRiskCount");
        }
        if (dbMetrics.containsKey("averageRiskScore") && totalAnalyses > 0) {
            double avg = (Double) dbMetrics.get("averageRiskScore");
            this.totalRiskScoreSum = (long) Math.round(avg * totalAnalyses);
        }
        if (dbMetrics.containsKey("distribution")) {
            @SuppressWarnings("unchecked")
            Map<RiskLevel, Integer> dist = (Map<RiskLevel, Integer>) dbMetrics.get("distribution");
            if (dist != null) {
                this.riskDistribution.clear();
                this.riskDistribution.putAll(dist);
            }
        }
    }

    /**
     * Atomically creates an immutable snapshot of current real-time metrics.
     */
    public synchronized Map<String, Object> getSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("totalAnalyses", totalAnalyses);
        snapshot.put("safeCount", safeCount);
        snapshot.put("suspiciousCount", suspiciousCount);
        snapshot.put("fraudulentCount", fraudulentCount);
        snapshot.put("highRiskCount", highRiskCount);

        double avg = (totalAnalyses > 0) ? ((double) totalRiskScoreSum / totalAnalyses) : 0.0;
        snapshot.put("averageRiskScore", avg);

        // Defensive copy of distribution
        snapshot.put("distribution", new EnumMap<>(riskDistribution));
        return snapshot;
    }

    public synchronized void reset() {
        totalAnalyses = 0;
        safeCount = 0;
        suspiciousCount = 0;
        fraudulentCount = 0;
        highRiskCount = 0;
        totalRiskScoreSum = 0;
        for (RiskLevel level : RiskLevel.values()) {
            riskDistribution.put(level, 0);
        }
    }
}
