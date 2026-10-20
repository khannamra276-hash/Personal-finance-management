package com.financeapp.ui;

import com.financeapp.model.Goal;
import com.financeapp.model.GoalStatus;
import com.financeapp.service.GoalService;
import com.financeapp.util.InputValidator;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GoalDialog extends JDialog {
    private final Long userId;
    private final Goal existingGoal;
    private final GoalService goalService;

    private final JTextField titleField;
    private final JTextField targetField;
    private final JTextField savedField;
    private final JTextField deadlineField;
    private final JComboBox<GoalStatus> statusCombo;

    private boolean saved = false;

    public GoalDialog(Frame parent, Long userId, Goal existingGoal, GoalService goalService) {
        super(parent, existingGoal == null ? "Create Savings Goal" : "Edit Savings Goal", true);
        this.userId = userId;
        this.existingGoal = existingGoal;
        this.goalService = goalService;

        setSize(440, 310);
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
        formPanel.add(new JLabel("Goal Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        titleField = new JTextField(20);
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Target Amount ($):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        targetField = new JTextField(15);
        formPanel.add(targetField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Currently Saved ($):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        savedField = new JTextField("0.00", 15);
        formPanel.add(savedField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Deadline (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        deadlineField = new JTextField(LocalDate.now().plusMonths(6).toString(), 15);
        formPanel.add(deadlineField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        statusCombo = new JComboBox<>(GoalStatus.values());
        formPanel.add(statusCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Goal");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        if (existingGoal != null) {
            populateFields(existingGoal);
        }
    }

    private void populateFields(Goal goal) {
        titleField.setText(goal.getTitle());
        targetField.setText(goal.getTargetAmount().toPlainString());
        savedField.setText(goal.getSavedAmount().toPlainString());
        deadlineField.setText(goal.getDeadline().toString());
        statusCombo.setSelectedItem(goal.getStatus());
    }

    private void handleSave() {
        String title = titleField.getText().trim();
        String targetText = targetField.getText().trim();
        String savedText = savedField.getText().trim();
        String deadlineText = deadlineField.getText().trim();
        GoalStatus status = (GoalStatus) statusCombo.getSelectedItem();

        if (!InputValidator.isNotEmpty(title)) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal target = InputValidator.parsePositiveAmount(targetText);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive target amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal savedAmt;
        try {
            savedAmt = new BigDecimal(savedText);
            if (savedAmt.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Saved amount cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid saved amount format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Goal goal = existingGoal != null ? existingGoal : new Goal();
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setTargetAmount(target);
        goal.setSavedAmount(savedAmt);
        goal.setDeadline(deadline);
        goal.setStatus(status != null ? status : GoalStatus.IN_PROGRESS);

        try {
            if (existingGoal == null) {
                goalService.createGoal(goal);
            } else {
                goalService.updateGoal(goal);
            }
            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

