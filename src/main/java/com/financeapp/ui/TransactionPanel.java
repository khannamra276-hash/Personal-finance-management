package com.financeapp.ui;

import com.financeapp.dao.CategoryDao;
import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.model.Transaction;
import com.financeapp.service.CsvExportService;
import com.financeapp.service.TransactionService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TransactionPanel extends JPanel {
    private final Long userId;
    private final Runnable onChangeCallback;
    private final TransactionService transactionService;
    private final CategoryDao categoryDao;
    private final CsvExportService csvExportService;

    private final JComboBox<String> typeFilterCombo;
    private final JComboBox<Object> categoryFilterCombo;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JTextField searchField;

    private final JTable transactionTable;
    private final DefaultTableModel tableModel;
    private List<Transaction> currentTransactions = new ArrayList<>();

    public TransactionPanel(Long userId, Runnable onChangeCallback) {
        this.userId = userId;
        this.onChangeCallback = onChangeCallback;
        this.transactionService = new TransactionService();
        this.categoryDao = new CategoryDao();
        this.csvExportService = new CsvExportService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBorder(UITheme.createCardBorder());

        filterBar.add(new JLabel("Type:"));
        typeFilterCombo = new JComboBox<>(new String[]{"All", "INCOME", "EXPENSE"});
        filterBar.add(typeFilterCombo);

        filterBar.add(new JLabel("Category:"));
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.addItem("All Categories");
        filterBar.add(categoryFilterCombo);

        filterBar.add(new JLabel("From:"));
        startDateField = new JTextField(7);
        filterBar.add(startDateField);

        filterBar.add(new JLabel("To:"));
        endDateField = new JTextField(7);
        filterBar.add(endDateField);

        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(10);
        filterBar.add(searchField);

        JButton filterBtn = new JButton("Apply Filter");
        JButton resetBtn = new JButton("Reset");
        filterBtn.addActionListener(e -> refreshData());
        resetBtn.addActionListener(e -> resetFilters());

        filterBar.add(filterBtn);
        filterBar.add(resetBtn);

        add(filterBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "Date", "Type", "Category", "Amount ($)", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(26);
        transactionTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        setupColumnRenderers();

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton addBtn = new JButton("Add Transaction");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton exportBtn = new JButton("Export to CSV");

        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> openTransactionDialog(null));
        editBtn.addActionListener(e -> editSelectedTransaction());
        deleteBtn.addActionListener(e -> deleteSelectedTransaction());
        exportBtn.addActionListener(e -> exportToCsv());

        actionBar.add(addBtn);
        actionBar.add(editBtn);
        actionBar.add(deleteBtn);
        actionBar.add(exportBtn);

        add(actionBar, BorderLayout.SOUTH);

        loadCategories();
        refreshData();
    }

    private void setupColumnRenderers() {
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && row < currentTransactions.size()) {
                    Transaction t = currentTransactions.get(row);
                    if (t.getType() == CategoryType.INCOME) {
                        c.setForeground(UITheme.INCOME_COLOR);
                    } else {
                        c.setForeground(UITheme.EXPENSE_COLOR);
                    }
                }
                return c;
            }
        };
        transactionTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
    }

    private void loadCategories() {
        try {
            List<Category> list = categoryDao.findByUserId(userId);
            categoryFilterCombo.removeAllItems();
            categoryFilterCombo.addItem("All Categories");
            for (Category c : list) {
                categoryFilterCombo.addItem(c);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        CategoryType type = null;
        String typeSel = (String) typeFilterCombo.getSelectedItem();
        if ("INCOME".equals(typeSel)) type = CategoryType.INCOME;
        else if ("EXPENSE".equals(typeSel)) type = CategoryType.EXPENSE;

        Long categoryId = null;
        Object catSel = categoryFilterCombo.getSelectedItem();
        if (catSel instanceof Category cat) {
            categoryId = cat.getId();
        }

        LocalDate startDate = parseDateOrNull(startDateField.getText().trim());
        LocalDate endDate = parseDateOrNull(endDateField.getText().trim());
        String search = searchField.getText().trim();

        try {
            currentTransactions = transactionService.getFilteredTransactions(userId, type, categoryId, startDate, endDate, search);
            tableModel.setRowCount(0);
            for (Transaction t : currentTransactions) {
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getTransactionDate(),
                        t.getType(),
                        t.getCategoryName(),
                        String.format("%.2f", t.getAmount()),
                        t.getDescription()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseDateOrNull(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void resetFilters() {
        typeFilterCombo.setSelectedIndex(0);
        categoryFilterCombo.setSelectedIndex(0);
        startDateField.setText("");
        endDateField.setText("");
        searchField.setText("");
        refreshData();
    }

    private void openTransactionDialog(Transaction txn) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        TransactionDialog dialog = new TransactionDialog(parentFrame, userId, txn, transactionService, categoryDao);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            if (onChangeCallback != null) {
                onChangeCallback.run();
            }
        }
    }

    private void editSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Transaction selected = currentTransactions.get(selectedRow);
        openTransactionDialog(selected);
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Transaction selected = currentTransactions.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete transaction #" + selected.getId() + " (" + selected.getDescription() + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionService.deleteTransaction(selected.getId(), userId);
                refreshData();
                if (onChangeCallback != null) {
                    onChangeCallback.run();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv() {
        if (currentTransactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions available to export.", "Empty Table", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions to CSV");
        fileChooser.setSelectedFile(new File("transactions_" + LocalDate.now() + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            try {
                csvExportService.exportTransactionsToCsv(currentTransactions, file);
                JOptionPane.showMessageDialog(this, "Transactions successfully exported to:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

