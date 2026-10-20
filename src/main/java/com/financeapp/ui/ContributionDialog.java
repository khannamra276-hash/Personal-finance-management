package com.financeapp.ui;

import com.financeapp.model.Goal;
import com.financeapp.service.GoalService;
import com.financeapp.util.InputValidator;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ContributionDialog extends JDialog {
    private final Long userId;
    private final Goal goal;
    private final GoalService goalService;
    private final JTextField amountField;
    private boolean contributed = false;

    public ContributionDialog(Frame parent, Long userId, Goal goal, GoalService goalService) {
        super(parent, "Add Contribution to Goal", true);
        this.userId = userId;
        this.goal = goal;
        this.goalService = goalService;

        setSize(400, 220);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(UITheme.createCardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Goal:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel goalTitleLbl = new JLabel(goal.getTitle());
        goalTitleLbl.setFont(UITheme.FONT_BOLD);
        formPanel.add(goalTitleLbl, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Current Saved / Target:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel statusLbl = new JLabel(String.format("$%s of $%s (%d%%)",
                goal.getSavedAmount().toPlainString(),
                goal.getTargetAmount().toPlainString(),
                goal.getProgressPercentage()));
        formPanel.add(statusLbl, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Contribution Amount ($):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField(12);
        formPanel.add(amountField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton submitBtn = new JButton("Add Funds");
        JButton cancelBtn = new JButton("Cancel");

        submitBtn.addActionListener(e -> handleContribution());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleContribution() {
        BigDecimal amount = InputValidator.parsePositiveAmount(amountField.getText().trim());
        if (amount == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive contribution amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            goalService.addContribution(goal.getId(), userId, amount);
            contributed = true;
            JOptionPane.showMessageDialog(this, "Contribution of $" + amount.toPlainString() + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isContributed() {
        return contributed;
    }
}

