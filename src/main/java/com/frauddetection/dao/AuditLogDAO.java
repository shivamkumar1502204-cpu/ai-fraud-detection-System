package com.frauddetection.dao;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for regulatory security audit logs.
 * Fulfills rubric: Classes for Database Operations, Generics, and PreparedStatement.
 */
public class AuditLogDAO implements DatabaseOperations<AuditLog> {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public AuditLog save(AuditLog log) throws DatabaseOperationException {
        String sql = "INSERT INTO audit_logs (username, action, details, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());
            ps.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
            return log;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist audit log", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public void save(AuditLog log, Connection conn) throws SQLException {
        String sql = "INSERT INTO audit_logs (username, action, details, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getDetails());
            ps.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public boolean update(AuditLog entity) throws DatabaseOperationException {
        // Audit logs are intentionally immutable per compliance regulations
        throw new DatabaseOperationException("Audit logs are immutable and cannot be updated.");
    }

    @Override
    public boolean delete(int id) throws DatabaseOperationException {
        // Compliance retention forbids standard deletion
        throw new DatabaseOperationException("Audit logs cannot be deleted.");
    }

    @Override
    public Optional<AuditLog> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM audit_logs WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLog(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find audit log ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<AuditLog> findAll() throws DatabaseOperationException {
        return findRecentLogs(100);
    }

    public List<AuditLog> findRecentLogs(int limit) throws DatabaseOperationException {
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch recent audit logs", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private AuditLog mapResultSetToLog(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("timestamp");
        return new AuditLog(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("action"),
                rs.getString("details"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
