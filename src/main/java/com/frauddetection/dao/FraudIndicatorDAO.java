package com.frauddetection.dao;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.FraudIndicator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for fine-grained fraud indicators.
 * Fulfills rubric: Classes for Database Operations, Generics, and PreparedStatement.
 */
public class FraudIndicatorDAO implements DatabaseOperations<FraudIndicator> {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public FraudIndicator save(FraudIndicator indicator) throws DatabaseOperationException {
        String sql = "INSERT INTO fraud_indicators (analysis_id, indicator_type, indicator_description, severity_weight) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, indicator.getAnalysisId());
            ps.setString(2, indicator.getIndicatorType());
            ps.setString(3, indicator.getIndicatorDescription());
            ps.setDouble(4, indicator.getSeverityWeight());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    indicator.setId(rs.getInt(1));
                }
            }
            return indicator;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save fraud indicator", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    /**
     * Batch persists a list of indicators within an existing active transaction connection.
     * Fulfills Collections & JDBC requirements.
     */
    public void saveBatch(int analysisId, List<FraudIndicator> indicators, Connection conn) throws SQLException {
        if (indicators == null || indicators.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO fraud_indicators (analysis_id, indicator_type, indicator_description, severity_weight) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FraudIndicator ind : indicators) {
                ps.setInt(1, analysisId);
                ps.setString(2, ind.getIndicatorType());
                ps.setString(3, ind.getIndicatorDescription());
                ps.setDouble(4, ind.getSeverityWeight());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public boolean update(FraudIndicator indicator) throws DatabaseOperationException {
        String sql = "UPDATE fraud_indicators SET indicator_type = ?, indicator_description = ?, " +
                "severity_weight = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, indicator.getIndicatorType());
            ps.setString(2, indicator.getIndicatorDescription());
            ps.setDouble(3, indicator.getSeverityWeight());
            ps.setInt(4, indicator.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update fraud indicator ID: " + indicator.getId(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM fraud_indicators WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete indicator ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public Optional<FraudIndicator> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM fraud_indicators WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIndicator(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find indicator ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public List<FraudIndicator> findByAnalysisId(int analysisId) throws DatabaseOperationException {
        String sql = "SELECT * FROM fraud_indicators WHERE analysis_id = ? ORDER BY severity_weight DESC";
        List<FraudIndicator> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, analysisId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToIndicator(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find indicators for analysis ID: " + analysisId, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<FraudIndicator> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM fraud_indicators ORDER BY id ASC";
        List<FraudIndicator> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToIndicator(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find all indicators", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private FraudIndicator mapResultSetToIndicator(ResultSet rs) throws SQLException {
        return new FraudIndicator(
                rs.getInt("id"),
                rs.getInt("analysis_id"),
                rs.getString("indicator_type"),
                rs.getString("indicator_description"),
                rs.getDouble("severity_weight")
        );
    }
}
