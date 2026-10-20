package com.financeapp.ui;

import com.financeapp.dao.CategoryDao;
import com.financeapp.model.Budget;
import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.service.BudgetService;
import com.financeapp.util.InputValidator;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BudgetDialog extends JDialog {
    private final Long userId;
    private final BudgetService budgetService;
    private final CategoryDao categoryDao;
    private final Budget existingBudget;

    private final JComboBox<Category> categoryCombo;
    private final JComboBox<Integer> monthCombo;
    private final JTextField yearField;
    private final JTextField limitField;

    private boolean saved = false;

    public BudgetDialog(Frame parent, Long userId, Budget existingBudget,
                        BudgetService budgetService, CategoryDao categoryDao, int defaultMonth, int defaultYear) {
        super(parent, existingBudget == null ? "Set Monthly Budget" : "Edit Budget Limit", true);
        this.userId = userId;
        this.existingBudget = existingBudget;
        this.budgetService = budgetService;
        this.categoryDao = categoryDao;

        setSize(420, 260);
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
        formPanel.add(new JLabel("Expense Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryCombo = new JComboBox<>();
        formPanel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Month:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(defaultMonth);
        formPanel.add(monthCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Year:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        yearField = new JTextField(String.valueOf(defaultYear), 10);
        formPanel.add(yearField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Limit Amount ($):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        limitField = new JTextField(10);
        formPanel.add(limitField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Budget");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loadExpenseCategories();

        if (existingBudget != null) {
            populateFields(existingBudget);
        }
    }

    private void loadExpenseCategories() {
        try {
            List<Category> list = categoryDao.findByUserIdAndType(userId, CategoryType.EXPENSE);
            categoryCombo.setModel(new DefaultComboBoxModel<>(list.toArray(new Category[0])));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields(Budget budget) {
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            Category c = categoryCombo.getItemAt(i);
            if (c.getId().equals(budget.getCategoryId())) {
                categoryCombo.setSelectedIndex(i);
                categoryCombo.setEnabled(false); // don't change category on edit
                break;
            }
        }
        monthCombo.setSelectedItem(budget.getMonth());
        monthCombo.setEnabled(false);
        yearField.setText(String.valueOf(budget.getYear()));
        yearField.setEditable(false);
        limitField.setText(budget.getLimitAmount().toPlainString());
    }

    private void handleSave() {
        Category category = (Category) categoryCombo.getSelectedItem();
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Please select an expense category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer month = (Integer) monthCombo.getSelectedItem();
        int year;
        try {
            year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 4-digit year.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal limit = InputValidator.parsePositiveAmount(limitField.getText().trim());
        if (limit == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive budget limit.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Budget budget = existingBudget != null ? existingBudget : new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(category.getId());
        budget.setCategoryName(category.getName());
        budget.setMonth(month != null ? month : LocalDate.now().getMonthValue());
        budget.setYear(year);
        budget.setLimitAmount(limit);

        try {
            budgetService.saveBudget(budget);
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

