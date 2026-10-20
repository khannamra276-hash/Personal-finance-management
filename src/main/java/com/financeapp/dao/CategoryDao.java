package com.financeapp.dao;

import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao {

    public Optional<Category> findById(Long id) throws SQLException {
        String sql = "SELECT id, user_id, name, type FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Category> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT id, user_id, name, type FROM categories WHERE user_id = ? ORDER BY name ASC";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        }
        return categories;
    }

    public List<Category> findByUserIdAndType(Long userId, CategoryType type) throws SQLException {
        String sql = "SELECT id, user_id, name, type FROM categories WHERE user_id = ? AND type = ? ORDER BY name ASC";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        }
        return categories;
    }

    public Category create(Category category) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, category.getUserId());
            stmt.setString(2, category.getName().trim());
            stmt.setString(3, category.getType().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getLong(1));
                }
            }
        }
        return category;
    }

    public boolean delete(Long id, Long userId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                CategoryType.valueOf(rs.getString("type"))
        );
    }
}

