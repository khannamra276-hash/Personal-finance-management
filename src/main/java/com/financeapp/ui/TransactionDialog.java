package com.financeapp.ui;

import com.financeapp.dao.CategoryDao;
import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.model.Transaction;
import com.financeapp.service.BudgetWarning;
import com.financeapp.service.TransactionService;
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
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TransactionDialog extends JDialog {
    private final Long userId;
    private final TransactionService transactionService;
    private final CategoryDao categoryDao;
    private final Transaction existingTransaction;

    private final JComboBox<CategoryType> typeCombo;
    private final JComboBox<Category> categoryCombo;
    private final JTextField amountField;
    private final JTextField dateField;
    private final JTextField descriptionField;

    private boolean saved = false;

    public TransactionDialog(Frame parent, Long userId, Transaction existingTransaction,
                             TransactionService transactionService, CategoryDao categoryDao) {
        super(parent, existingTransaction == null ? "Add Transaction" : "Edit Transaction", true);
        this.userId = userId;
        this.existingTransaction = existingTransaction;
        this.transactionService = transactionService;
        this.categoryDao = categoryDao;

        setSize(440, 320);
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
        formPanel.add(new JLabel("Type:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        typeCombo = new JComboBox<>(CategoryType.values());
        formPanel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryCombo = new JComboBox<>();
        formPanel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Amount ($):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateField = new JTextField(LocalDate.now().toString(), 15);
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descriptionField = new JTextField(20);
        formPanel.add(descriptionField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Transaction");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        typeCombo.addActionListener(e -> reloadCategories());
        reloadCategories();

        if (existingTransaction != null) {
            populateFields(existingTransaction);
        }
    }

    private void reloadCategories() {
        CategoryType selectedType = (CategoryType) typeCombo.getSelectedItem();
        if (selectedType == null) return;
        try {
            List<Category> categories = categoryDao.findByUserIdAndType(userId, selectedType);
            categoryCombo.setModel(new DefaultComboBoxModel<>(categories.toArray(new Category[0])));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields(Transaction txn) {
        typeCombo.setSelectedItem(txn.getType());
        reloadCategories();
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            Category c = categoryCombo.getItemAt(i);
            if (c.getId().equals(txn.getCategoryId())) {
                categoryCombo.setSelectedIndex(i);
                break;
            }
        }
        amountField.setText(txn.getAmount().toPlainString());
        dateField.setText(txn.getTransactionDate().toString());
        descriptionField.setText(txn.getDescription());
    }

    private void handleSave() {
        CategoryType type = (CategoryType) typeCombo.getSelectedItem();
        Category category = (Category) categoryCombo.getSelectedItem();
        String amountText = amountField.getText().trim();
        String dateText = dateField.getText().trim();
        String description = descriptionField.getText().trim();

        if (category == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount = InputValidator.parsePositiveAmount(amountText);
        if (amount == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate txnDate;
        try {
            txnDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isNotEmpty(description)) {
            JOptionPane.showMessageDialog(this, "Description cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Transaction txn = existingTransaction != null ? existingTransaction : new Transaction();
        txn.setUserId(userId);
        txn.setCategoryId(category.getId());
        txn.setCategoryName(category.getName());
        txn.setType(type);
        txn.setAmount(amount);
        txn.setTransactionDate(txnDate);
        txn.setDescription(description);

        try {
            transactionService.saveTransaction(txn);
            saved = true;

            // Trigger budget warning check if this is an expense
            if (type == CategoryType.EXPENSE) {
                Optional<BudgetWarning> warningOpt = transactionService.checkBudgetThreshold(txn);
                if (warningOpt.isPresent()) {
                    BudgetWarning warning = warningOpt.get();
                    int msgType = warning.exceededLimit() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE;
                    JOptionPane.showMessageDialog(this, warning.getFormattedMessage(), "Budget Threshold Alert", msgType);
                }
            }

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

