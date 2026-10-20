package com.financeapp.dao;

import com.financeapp.model.CategoryType;
import com.financeapp.model.Transaction;
import com.financeapp.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionDao {

    public Optional<Transaction> findById(Long id, Long userId) throws SQLException {
        String sql = """
            SELECT t.id, t.user_id, t.category_id, c.name AS category_name,
                   t.amount, t.type, t.description, t.txn_date, t.created_at
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.id = ? AND t.user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findByFilters(Long userId, CategoryType type, Long categoryId,
                                          LocalDate startDate, LocalDate endDate, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT t.id, t.user_id, t.category_id, c.name AS category_name,
                   t.amount, t.type, t.description, t.txn_date, t.created_at
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null) {
            sql.append(" AND t.type = ?");
            params.add(type.name());
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (startDate != null) {
            sql.append(" AND t.txn_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND t.txn_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(t.description) LIKE ?");
            params.add("%" + search.trim().toLowerCase() + "%");
        }

        sql.append(" ORDER BY t.txn_date DESC, t.id DESC");

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    public Transaction create(Transaction txn) throws SQLException {
        String sql = """
            INSERT INTO transactions (user_id, category_id, amount, type, description, txn_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, txn.getUserId());
            stmt.setLong(2, txn.getCategoryId());
            stmt.setBigDecimal(3, txn.getAmount());
            stmt.setString(4, txn.getType().name());
            stmt.setString(5, txn.getDescription().trim());
            stmt.setDate(6, Date.valueOf(txn.getTransactionDate()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    txn.setId(keys.getLong(1));
                }
            }
        }
        return txn;
    }

    public boolean update(Transaction txn) throws SQLException {
        String sql = """
            UPDATE transactions
            SET category_id = ?, amount = ?, type = ?, description = ?, txn_date = ?
            WHERE id = ? AND user_id = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, txn.getCategoryId());
            stmt.setBigDecimal(2, txn.getAmount());
            stmt.setString(3, txn.getType().name());
            stmt.setString(4, txn.getDescription().trim());
            stmt.setDate(5, Date.valueOf(txn.getTransactionDate()));
            stmt.setLong(6, txn.getId());
            stmt.setLong(7, txn.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public BigDecimal getTotalAmountByTypeAndMonth(Long userId, CategoryType type, int month, int year) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS total
            FROM transactions
            WHERE user_id = ? AND type = ? AND MONTH(txn_date) = ? AND YEAR(txn_date) = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, type.name());
            stmt.setInt(3, month);
            stmt.setInt(4, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> getExpenseBreakdownByCategory(Long userId, int month, int year) throws SQLException {
        String sql = """
            SELECT c.name AS category_name, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ? AND t.type = 'EXPENSE' AND MONTH(t.txn_date) = ? AND YEAR(t.txn_date) = ?
            GROUP BY c.id, c.name
            ORDER BY total DESC
            """;
        Map<String, BigDecimal> result = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category_name"), rs.getBigDecimal("total"));
                }
            }
        }
        return result;
    }

    public BigDecimal getCategorySpentInMonth(Long userId, Long categoryId, int month, int year) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) AS total
            FROM transactions
            WHERE user_id = ? AND category_id = ? AND type = 'EXPENSE'
              AND MONTH(txn_date) = ? AND YEAR(txn_date) = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, categoryId);
            stmt.setInt(3, month);
            stmt.setInt(4, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        Date txnDate = rs.getDate("txn_date");
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getBigDecimal("amount"),
                CategoryType.valueOf(rs.getString("type")),
                rs.getString("description"),
                txnDate != null ? txnDate.toLocalDate() : null,
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}

