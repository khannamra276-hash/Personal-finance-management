package com.financeapp.ui;

import com.financeapp.model.User;
import com.financeapp.service.AuthService;
import com.financeapp.util.DBConnection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final AuthService authService;

    public LoginFrame() {
        super("Online Personal Finance Management Platform - Login");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 420);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel titleLabel = new JLabel("Personal Finance Platform", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Track Income, Budgets, and Savings Goals", SwingConstants.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(new Color(227, 242, 253));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(UITheme.FONT_BOLD);
        centerPanel.add(emailLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField("alex@financeapp.com", 20);
        centerPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BOLD);
        centerPanel.add(passLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField("Password123", 20);
        centerPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 8, 8);
        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(UITheme.FONT_BOLD);
        loginButton.setBackground(UITheme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> handleLogin());
        centerPanel.add(loginButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 8, 8, 8);
        JButton registerButton = new JButton("Don't have an account? Register");
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setForeground(UITheme.PRIMARY);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> openRegisterDialog());
        centerPanel.add(registerButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton dbConfigBtn = new JButton("Database Settings");
        dbConfigBtn.setFont(UITheme.FONT_SMALL);
        dbConfigBtn.addActionListener(e -> new DbConfigDialog(this).setVisible(true));
        footerPanel.add(dbConfigBtn);
        add(footerPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(email, password);
            dispose();
            new MainFrame(user).setVisible(true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Database connection failed: " + ex.getMessage() + "\n\nWould you like to configure database credentials now?",
                    "Database Connection Error",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                DbConfigDialog dialog = new DbConfigDialog(this);
                dialog.setVisible(true);
            }
        }
    }

    private void openRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this, authService);
        dialog.setVisible(true);
        if (dialog.isRegistered()) {
            emailField.setText("");
            passwordField.setText("");
        }
    }
}

