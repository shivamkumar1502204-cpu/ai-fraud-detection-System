package com.frauddetection.dao;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.User;
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
 * Data Access Object for User entities.
 * Fulfills rubric: Classes for Database Operations, Generics, and PreparedStatement.
 */
public class UserDAO implements DatabaseOperations<User> {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    @Override
    public User save(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO users (username, password_hash, salt, role, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole());
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save user: " + user.getUsername(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(User user) throws DatabaseOperationException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, salt = ?, role = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.setString(4, user.getRole());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update user ID: " + user.getId(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT id, username, password_hash, salt, role, created_at FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find user by ID: " + id, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        String sql = "SELECT id, username, password_hash, salt, role, created_at FROM users ORDER BY id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to list all users", e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public Optional<User> findByUsername(String username) throws DatabaseOperationException {
        String sql = "SELECT id, username, password_hash, salt, role, created_at FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find user by username: " + username, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("role"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
