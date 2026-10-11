package com.frauddetection.dao;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.Decision;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.RiskLevel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Fraud Analysis evaluations.
 * Fulfills rubric: Classes for Database Operations, Generics, and PreparedStatement.
 */
public class FraudAnalysisDAO implements DatabaseOperations<FraudAnalysis> {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public FraudAnalysis save(FraudAnalysis fa) throws DatabaseOperationException {
        try (Connection conn = db.getConnection()) {
            return save(fa, conn);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist fraud analysis: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public FraudAnalysis save(FraudAnalysis fa, Connection conn) throws SQLException {
        String sql = "INSERT INTO fraud_analysis " +
                "(transaction_id, risk_score, risk_level, decision, recommendation, reasons_json, analysis_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fa.getTransactionId());
            ps.setInt(2, fa.getRiskScore());
            ps.setString(3, fa.getRiskLevel().name());
            ps.setString(4, fa.getDecision().name());
            ps.setString(5, fa.getRecommendation());
            ps.setString(6, String.join(" | ", fa.getReasons()));
            ps.setTimestamp(7, Timestamp.valueOf(fa.getAnalysisTime()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    fa.setId(rs.getInt(1));
                }
            }
            return fa;
        }
    }

    @Override
    public boolean update(FraudAnalysis fa) throws DatabaseOperationException {
        String sql = "UPDATE fraud_analysis SET risk_score = ?, risk_level = ?, decision = ?, " +
                "recommendation = ?, reasons_json = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fa.getRiskScore());
            ps.setString(2, fa.getRiskLevel().name());
            ps.setString(3, fa.getDecision().name());
            ps.setString(4, fa.getRecommendation());
            ps.setString(5, String.join(" | ", fa.getReasons()));
            ps.setInt(6, fa.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update fraud analysis ID: " + fa.getId(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM fraud_analysis WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete fraud analysis ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public Optional<FraudAnalysis> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT fa.*, t.transaction_id as tx_code FROM fraud_analysis fa " +
                "JOIN transactions t ON fa.transaction_id = t.id WHERE fa.id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAnalysis(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find fraud analysis ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public Optional<FraudAnalysis> findByTransactionId(int transactionId) throws DatabaseOperationException {
        String sql = "SELECT fa.*, t.transaction_id as tx_code FROM fraud_analysis fa " +
                "JOIN transactions t ON fa.transaction_id = t.id WHERE fa.transaction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAnalysis(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find analysis for transaction ID: " + transactionId, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<FraudAnalysis> findAll() throws DatabaseOperationException {
        String sql = "SELECT fa.*, t.transaction_id as tx_code FROM fraud_analysis fa " +
                "JOIN transactions t ON fa.transaction_id = t.id ORDER BY fa.analysis_time DESC";
        List<FraudAnalysis> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAnalysis(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load all fraud analyses", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    /**
     * Aggregates real-time statistics directly from database for the dashboard.
     * Fulfills Collections requirement: returns Map<String, Object>.
     */
    public Map<String, Object> getRiskStatistics() throws DatabaseOperationException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                "COUNT(*) AS total_analyses, " +
                "COALESCE(AVG(risk_score), 0.0) AS avg_score, " +
                "SUM(CASE WHEN decision = 'SAFE' THEN 1 ELSE 0 END) AS safe_count, " +
                "SUM(CASE WHEN decision = 'SUSPICIOUS' THEN 1 ELSE 0 END) AS suspicious_count, " +
                "SUM(CASE WHEN decision = 'FRAUDULENT' THEN 1 ELSE 0 END) AS fraudulent_count, " +
                "SUM(CASE WHEN risk_level = 'LOW' THEN 1 ELSE 0 END) AS low_risk_count, " +
                "SUM(CASE WHEN risk_level = 'MEDIUM' THEN 1 ELSE 0 END) AS med_risk_count, " +
                "SUM(CASE WHEN risk_level = 'HIGH' THEN 1 ELSE 0 END) AS high_risk_count, " +
                "SUM(CASE WHEN risk_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_risk_count " +
                "FROM fraud_analysis";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("totalAnalyses", rs.getInt("total_analyses"));
                stats.put("averageRiskScore", rs.getDouble("avg_score"));
                stats.put("safeCount", rs.getInt("safe_count"));
                stats.put("suspiciousCount", rs.getInt("suspicious_count"));
                stats.put("fraudulentCount", rs.getInt("fraudulent_count"));
                stats.put("highRiskCount", rs.getInt("high_risk_count") + rs.getInt("critical_risk_count"));

                Map<RiskLevel, Integer> distribution = new EnumMap<>(RiskLevel.class);
                distribution.put(RiskLevel.LOW, rs.getInt("low_risk_count"));
                distribution.put(RiskLevel.MEDIUM, rs.getInt("med_risk_count"));
                distribution.put(RiskLevel.HIGH, rs.getInt("high_risk_count"));
                distribution.put(RiskLevel.CRITICAL, rs.getInt("critical_risk_count"));
                stats.put("distribution", distribution);
            }

            // Total transactions count
            String txCountSql = "SELECT COUNT(*) FROM transactions";
            try (PreparedStatement ps2 = conn.prepareStatement(txCountSql);
                 ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    stats.put("totalTransactions", rs2.getInt(1));
                }
            }

            return stats;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query risk statistics", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private FraudAnalysis mapResultSetToAnalysis(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("analysis_time");
        String reasonsStr = rs.getString("reasons_json");
        List<String> reasons = new ArrayList<>();
        if (reasonsStr != null && !reasonsStr.isEmpty()) {
            for (String part : reasonsStr.split("\\|")) {
                if (!part.trim().isEmpty()) {
                    reasons.add(part.trim());
                }
            }
        }

        RiskLevel rl;
        try {
            rl = RiskLevel.valueOf(rs.getString("risk_level"));
        } catch (Exception e) {
            rl = RiskLevel.fromScore(rs.getInt("risk_score"));
        }

        Decision dec;
        try {
            dec = Decision.valueOf(rs.getString("decision"));
        } catch (Exception e) {
            dec = Decision.fromScore(rs.getInt("risk_score"));
        }

        String txCode = null;
        try {
            txCode = rs.getString("tx_code");
        } catch (SQLException ignored) {
        }

        return new FraudAnalysis(
                rs.getInt("id"),
                rs.getInt("transaction_id"),
                txCode,
                rs.getInt("risk_score"),
                rl,
                dec,
                rs.getString("recommendation"),
                reasons,
                new ArrayList<>(),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
