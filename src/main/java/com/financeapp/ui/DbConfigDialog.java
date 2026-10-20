package com.financeapp.ui;

import com.financeapp.util.DBConnection;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class DbConfigDialog extends JDialog {
    private final JTextField urlField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private boolean connected = false;

    public DbConfigDialog(Frame parent) {
        super(parent, "Database Configuration", true);
        setSize(480, 260);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("JDBC URL:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String currentUrl = DBConnection.getProperty("db.url");
        urlField = new JTextField(currentUrl != null ? currentUrl : "jdbc:mysql://localhost:3306/personal_finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        formPanel.add(urlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String currentUser = DBConnection.getProperty("db.user");
        userField = new JTextField(currentUser != null ? currentUser : "root");
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String currentPass = DBConnection.getProperty("db.password");
        passwordField = new JPasswordField(currentPass != null ? currentPass : "");
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton testButton = new JButton("Test Connection");
        JButton saveButton = new JButton("Save & Connect");
        JButton cancelButton = new JButton("Cancel");

        testButton.addActionListener(e -> testConnection());
        saveButton.addActionListener(e -> saveAndConnect());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void testConnection() {
        String url = urlField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passwordField.getPassword());
        DBConnection.setCredentials(url, user, pass);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn.isValid(3)) {
                JOptionPane.showMessageDialog(this, "Connection successful!", "Database", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndConnect() {
        String url = urlField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passwordField.getPassword());
        DBConnection.setCredentials(url, user, pass);

        try {
            DBConnection.initializeDatabase();
            connected = true;
            JOptionPane.showMessageDialog(this, "Database initialized and ready.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to initialize database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}

