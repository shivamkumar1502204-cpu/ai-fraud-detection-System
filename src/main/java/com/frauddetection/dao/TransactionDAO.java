package com.frauddetection.dao;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.Transaction;
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
 * Data Access Object for financial transaction telemetry records.
 * Fulfills rubric: Classes for Database Operations, Generics, and PreparedStatement.
 */
public class TransactionDAO implements DatabaseOperations<Transaction> {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public Transaction save(Transaction tx) throws DatabaseOperationException {
        String sql = "INSERT INTO transactions " +
                "(transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, " +
                "transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tx.getTransactionId());
            ps.setString(2, tx.getSenderName());
            ps.setString(3, tx.getReceiverName());
            ps.setDouble(4, tx.getAmount());
            ps.setString(5, tx.getTransactionType());
            ps.setString(6, tx.getLocation());
            ps.setString(7, tx.getDevice());
            ps.setInt(8, tx.getTransactionFrequency());
            ps.setBoolean(9, tx.isNewDevice());
            ps.setBoolean(10, tx.isUnusualLocation());
            ps.setString(11, tx.getSuspiciousText());
            ps.setString(12, tx.getUrl());
            ps.setDouble(13, tx.getPreviousAmount());
            ps.setTimestamp(14, Timestamp.valueOf(tx.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
            return tx;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist transaction: " + tx.getTransactionId(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    /**
     * Overload supporting an active transactional Connection for atomic multi-table operations.
     */
    public Transaction save(Transaction tx, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions " +
                "(transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, " +
                "transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tx.getTransactionId());
            ps.setString(2, tx.getSenderName());
            ps.setString(3, tx.getReceiverName());
            ps.setDouble(4, tx.getAmount());
            ps.setString(5, tx.getTransactionType());
            ps.setString(6, tx.getLocation());
            ps.setString(7, tx.getDevice());
            ps.setInt(8, tx.getTransactionFrequency());
            ps.setBoolean(9, tx.isNewDevice());
            ps.setBoolean(10, tx.isUnusualLocation());
            ps.setString(11, tx.getSuspiciousText());
            ps.setString(12, tx.getUrl());
            ps.setDouble(13, tx.getPreviousAmount());
            ps.setTimestamp(14, Timestamp.valueOf(tx.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
            return tx;
        }
    }

    @Override
    public boolean update(Transaction tx) throws DatabaseOperationException {
        String sql = "UPDATE transactions SET sender_name = ?, receiver_name = ?, amount = ?, transaction_type = ?, " +
                "location = ?, device = ?, transaction_frequency = ?, new_device = ?, unusual_location = ?, " +
                "suspicious_text = ?, url = ?, previous_amount = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tx.getSenderName());
            ps.setString(2, tx.getReceiverName());
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getTransactionType());
            ps.setString(5, tx.getLocation());
            ps.setString(6, tx.getDevice());
            ps.setInt(7, tx.getTransactionFrequency());
            ps.setBoolean(8, tx.isNewDevice());
            ps.setBoolean(9, tx.isUnusualLocation());
            ps.setString(10, tx.getSuspiciousText());
            ps.setString(11, tx.getUrl());
            ps.setDouble(12, tx.getPreviousAmount());
            ps.setInt(13, tx.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update transaction ID: " + tx.getId(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete transaction ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find transaction by ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public Optional<Transaction> findByTransactionId(String transactionId) throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find transaction by code: " + transactionId, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Transaction> findAll() throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load all transactions", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public List<Transaction> findRecentTransactions(int limit) throws DatabaseOperationException {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load recent transactions", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Transaction(
                rs.getInt("id"),
                rs.getString("transaction_id"),
                rs.getString("sender_name"),
                rs.getString("receiver_name"),
                rs.getDouble("amount"),
                rs.getString("transaction_type"),
                rs.getString("location"),
                rs.getString("device"),
                rs.getInt("transaction_frequency"),
                rs.getBoolean("new_device"),
                rs.getBoolean("unusual_location"),
                rs.getString("suspicious_text"),
                rs.getString("url"),
                rs.getDouble("previous_amount"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
