package com.financeapp.dao;

import com.financeapp.model.Budget;
import com.financeapp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDao {

    public List<Budget> findByUserAndMonth(Long userId, int month, int year) throws SQLException {
        String sql = """
            SELECT b.id, b.user_id, b.category_id, c.name AS category_name,
                   b.month, b.year, b.limit_amount
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.user_id = ? AND b.month = ? AND b.year = ?
            ORDER BY c.name ASC
            """;
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBudget(rs));
                }
            }
        }
        return list;
    }

    public Optional<Budget> findByCategoryAndMonth(Long userId, Long categoryId, int month, int year) throws SQLException {
        String sql = """
            SELECT b.id, b.user_id, b.category_id, c.name AS category_name,
                   b.month, b.year, b.limit_amount
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.user_id = ? AND b.category_id = ? AND b.month = ? AND b.year = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, categoryId);
            stmt.setInt(3, month);
            stmt.setInt(4, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Budget saveOrUpdate(Budget budget) throws SQLException {
        String sql = """
            INSERT INTO budgets (user_id, category_id, month, year, limit_amount)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE limit_amount = VALUES(limit_amount)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, budget.getUserId());
            stmt.setLong(2, budget.getCategoryId());
            stmt.setInt(3, budget.getMonth());
            stmt.setInt(4, budget.getYear());
            stmt.setBigDecimal(5, budget.getLimitAmount());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    budget.setId(keys.getLong(1));
                }
            }
        }
        return budget;
    }

    public boolean delete(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        return new Budget(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getInt("month"),
                rs.getInt("year"),
                rs.getBigDecimal("limit_amount")
        );
    }
}

