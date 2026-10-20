package com.financeapp;

import com.financeapp.ui.LoginFrame;
import com.financeapp.util.DBConnection;
import com.formdev.flatlaf.FlatLightLaf;

import java.sql.SQLException;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        try {
            DBConnection.initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Note: Database not auto-initialized on startup: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

