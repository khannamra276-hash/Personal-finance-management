package com.financeapp.service;

import com.financeapp.dao.BudgetDao;
import com.financeapp.dao.CategoryDao;
import com.financeapp.dao.TransactionDao;
import com.financeapp.model.Budget;
import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.model.Transaction;
import com.financeapp.util.InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionService {
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final CategoryDao categoryDao;

    public TransactionService() {
        this.transactionDao = new TransactionDao();
        this.budgetDao = new BudgetDao();
        this.categoryDao = new CategoryDao();
    }

    public TransactionService(TransactionDao transactionDao, BudgetDao budgetDao, CategoryDao categoryDao) {
        this.transactionDao = transactionDao;
        this.budgetDao = budgetDao;
        this.categoryDao = categoryDao;
    }

    public List<Transaction> getFilteredTransactions(Long userId, CategoryType type, Long categoryId,
                                                    LocalDate startDate, LocalDate endDate, String search) throws SQLException {
        return transactionDao.findByFilters(userId, type, categoryId, startDate, endDate, search);
    }

    public Transaction saveTransaction(Transaction txn) throws SQLException {
        if (txn.getAmount() == null || txn.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (!InputValidator.isNotEmpty(txn.getDescription())) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (txn.getTransactionDate() == null) {
            throw new IllegalArgumentException("Transaction date is required.");
        }
        if (txn.getCategoryId() == null) {
            throw new IllegalArgumentException("Category must be selected.");
        }

        if (txn.getId() == null) {
            return transactionDao.create(txn);
        } else {
            transactionDao.update(txn);
            return txn;
        }
    }

    public boolean deleteTransaction(Long id, Long userId) throws SQLException {
        return transactionDao.delete(id, userId);
    }

    public Optional<BudgetWarning> checkBudgetThreshold(Transaction expenseTxn) throws SQLException {
        if (expenseTxn.getType() != CategoryType.EXPENSE) {
            return Optional.empty();
        }

        int month = expenseTxn.getTransactionDate().getMonthValue();
        int year = expenseTxn.getTransactionDate().getYear();

        Optional<Budget> budgetOpt = budgetDao.findByCategoryAndMonth(
                expenseTxn.getUserId(), expenseTxn.getCategoryId(), month, year);

        if (budgetOpt.isEmpty()) {
            return Optional.empty();
        }

        Budget budget = budgetOpt.get();
        BigDecimal limit = budget.getLimitAmount();
        if (limit.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        BigDecimal currentSpent = transactionDao.getCategorySpentInMonth(
                expenseTxn.getUserId(), expenseTxn.getCategoryId(), month, year);

        BigDecimal percentage = currentSpent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

        boolean exceededLimit = currentSpent.compareTo(limit) >= 0;
        boolean exceededWarningThreshold = percentage.compareTo(BigDecimal.valueOf(80)) >= 0;

        if (exceededWarningThreshold) {
            String categoryName = budget.getCategoryName();
            if (categoryName == null || categoryName.isEmpty()) {
                categoryName = categoryDao.findById(expenseTxn.getCategoryId())
                        .map(Category::getName).orElse("Selected Category");
            }

            return Optional.of(new BudgetWarning(
                    exceededLimit,
                    exceededWarningThreshold,
                    limit,
                    currentSpent,
                    percentage,
                    categoryName
            ));
        }

        return Optional.empty();
    }
}

