package com.financeapp.dao;

import com.financeapp.model.Goal;
import com.financeapp.model.GoalStatus;
import com.financeapp.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoalDao {

    public List<Goal> findByUserId(Long userId) throws SQLException {
        String sql = """
            SELECT id, user_id, title, target_amount, saved_amount, deadline, status, created_at
            FROM goals
            WHERE user_id = ?
            ORDER BY status ASC, deadline ASC
            """;
        List<Goal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGoal(rs));
                }
            }
        }
        return list;
    }

    public Optional<Goal> findById(Long id, Long userId) throws SQLException {
        String sql = """
            SELECT id, user_id, title, target_amount, saved_amount, deadline, status, created_at
            FROM goals
            WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGoal(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Goal create(Goal goal) throws SQLException {
        String sql = """
            INSERT INTO goals (user_id, title, target_amount, saved_amount, deadline, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, goal.getUserId());
            stmt.setString(2, goal.getTitle().trim());
            stmt.setBigDecimal(3, goal.getTargetAmount());
            stmt.setBigDecimal(4, goal.getSavedAmount() != null ? goal.getSavedAmount() : BigDecimal.ZERO);
            stmt.setDate(5, Date.valueOf(goal.getDeadline()));
            stmt.setString(6, goal.getStatus().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setId(keys.getLong(1));
                }
            }
        }
        return goal;
    }

    public boolean update(Goal goal) throws SQLException {
        String sql = """
            UPDATE goals
            SET title = ?, target_amount = ?, saved_amount = ?, deadline = ?, status = ?
            WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, goal.getTitle().trim());
            stmt.setBigDecimal(2, goal.getTargetAmount());
            stmt.setBigDecimal(3, goal.getSavedAmount());
            stmt.setDate(4, Date.valueOf(goal.getDeadline()));
            stmt.setString(5, goal.getStatus().name());
            stmt.setLong(6, goal.getId());
            stmt.setLong(7, goal.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean addContribution(Long id, Long userId, BigDecimal contribution) throws SQLException {
        String sql = """
            UPDATE goals
            SET saved_amount = saved_amount + ?,
                status = CASE WHEN (saved_amount + ?) >= target_amount THEN 'COMPLETED' ELSE status END
            WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, contribution);
            stmt.setBigDecimal(2, contribution);
            stmt.setLong(3, id);
            stmt.setLong(4, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        Date deadline = rs.getDate("deadline");
        return new Goal(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("title"),
                rs.getBigDecimal("target_amount"),
                rs.getBigDecimal("saved_amount"),
                deadline != null ? deadline.toLocalDate() : null,
                GoalStatus.valueOf(rs.getString("status")),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}

