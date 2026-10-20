package com.financeapp.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {
    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Failed to load config.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/personal_finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String user = properties.getProperty("db.user", "root");
        String password = properties.getProperty("db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    public static void setCredentials(String url, String user, String password) {
        if (url != null) properties.setProperty("db.url", url);
        if (user != null) properties.setProperty("db.user", user);
        if (password != null) properties.setProperty("db.password", password);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static synchronized void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }

        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                StringBuilder sqlBatch = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sqlBatch.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String query = sqlBatch.toString().trim();
                        query = query.substring(0, query.length() - 1).trim();
                        if (!query.isEmpty()) {
                            try {
                                statement.execute(query);
                            } catch (SQLException ignored) {
                                // Ignore duplicate / exists errors during idempotent setup
                            }
                        }
                        sqlBatch.setLength(0);
                    }
                }
                initialized = true;
            }
        } catch (Exception e) {
            throw new SQLException("Database initialization failed: " + e.getMessage(), e);
        }
    }
}

