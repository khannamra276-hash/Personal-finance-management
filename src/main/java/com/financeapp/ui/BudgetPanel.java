package com.financeapp.ui;

import com.financeapp.dao.CategoryDao;
import com.financeapp.model.Budget;
import com.financeapp.service.BudgetService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BudgetPanel extends JPanel {
    private final Long userId;
    private final Runnable onChangeCallback;
    private final BudgetService budgetService;
    private final CategoryDao categoryDao;

    private final JComboBox<Integer> monthCombo;
    private final JTextField yearField;
    private final JTable budgetTable;
    private final DefaultTableModel tableModel;
    private List<Budget> currentBudgets = new ArrayList<>();

    public BudgetPanel(Long userId, Runnable onChangeCallback) {
        this.userId = userId;
        this.onChangeCallback = onChangeCallback;
        this.budgetService = new BudgetService();
        this.categoryDao = new CategoryDao();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Month Selector Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topBar.setBorder(UITheme.createCardBorder());

        topBar.add(new JLabel("Month:"));
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
        topBar.add(monthCombo);

        topBar.add(new JLabel("Year:"));
        yearField = new JTextField(String.valueOf(LocalDate.now().getYear()), 6);
        topBar.add(yearField);

        JButton loadBtn = new JButton("Load Period");
        loadBtn.addActionListener(e -> refreshData());
        topBar.add(loadBtn);

        add(topBar, BorderLayout.NORTH);

        // Budget Table
        String[] columns = {"ID", "Category", "Limit ($)", "Spent ($)", "Remaining ($)", "Usage %", "Progress"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        budgetTable = new JTable(tableModel);
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        budgetTable.setRowHeight(28);
        budgetTable.getTableHeader().setFont(UITheme.FONT_BOLD);

        setupTableRenderers();

        JScrollPane scrollPane = new JScrollPane(budgetTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton addBtn = new JButton("Set / Update Budget");
        JButton deleteBtn = new JButton("Delete Selected");

        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> openBudgetDialog());
        deleteBtn.addActionListener(e -> deleteSelectedBudget());

        actionBar.add(addBtn);
        actionBar.add(deleteBtn);
        add(actionBar, BorderLayout.SOUTH);

        refreshData();
    }

    private void setupTableRenderers() {
        budgetTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            private final JProgressBar progressBar = new JProgressBar(0, 100);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                int progress = value instanceof Number num ? num.intValue() : 0;
                progressBar.setValue(Math.min(100, Math.max(0, progress)));
                progressBar.setStringPainted(true);
                progressBar.setString(progress + "%");

                if (progress >= 100) {
                    progressBar.setForeground(UITheme.EXPENSE_COLOR);
                } else if (progress >= 80) {
                    progressBar.setForeground(UITheme.WARNING_COLOR);
                } else {
                    progressBar.setForeground(UITheme.INCOME_COLOR);
                }

                return progressBar;
            }
        });
    }

    public void refreshData() {
        int month = (Integer) monthCombo.getSelectedItem();
        int year;
        try {
            year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            year = LocalDate.now().getYear();
            yearField.setText(String.valueOf(year));
        }

        try {
            currentBudgets = budgetService.getMonthlyBudgetsWithSpending(userId, month, year);
            tableModel.setRowCount(0);
            for (Budget b : currentBudgets) {
                BigDecimal limit = b.getLimitAmount();
                BigDecimal spent = b.getSpentAmount();
                BigDecimal remaining = limit.subtract(spent);

                int percentage = 0;
                if (limit.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = spent.divide(limit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).intValue();
                }

                tableModel.addRow(new Object[]{
                        b.getId(),
                        b.getCategoryName(),
                        String.format("%.2f", limit),
                        String.format("%.2f", spent),
                        String.format("%.2f", remaining),
                        percentage + "%",
                        percentage
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load budgets: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openBudgetDialog() {
        int month = (Integer) monthCombo.getSelectedItem();
        int year = Integer.parseInt(yearField.getText().trim());
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        Budget selectedBudget = null;
        int row = budgetTable.getSelectedRow();
        if (row >= 0 && row < currentBudgets.size()) {
            selectedBudget = currentBudgets.get(row);
        }

        BudgetDialog dialog = new BudgetDialog(parentFrame, userId, selectedBudget, budgetService, categoryDao, month, year);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            if (onChangeCallback != null) {
                onChangeCallback.run();
            }
        }
    }

    private void deleteSelectedBudget() {
        int row = budgetTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a budget to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Budget selected = currentBudgets.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the budget for '" + selected.getCategoryName() + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                budgetService.deleteBudget(selected.getId(), userId);
                refreshData();
                if (onChangeCallback != null) {
                    onChangeCallback.run();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete budget: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

