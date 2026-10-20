package com.financeapp.service;

import com.financeapp.dao.BudgetDao;
import com.financeapp.dao.TransactionDao;
import com.financeapp.model.Budget;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BudgetService {
    private final BudgetDao budgetDao;
    private final TransactionDao transactionDao;

    public BudgetService() {
        this.budgetDao = new BudgetDao();
        this.transactionDao = new TransactionDao();
    }

    public BudgetService(BudgetDao budgetDao, TransactionDao transactionDao) {
        this.budgetDao = budgetDao;
        this.transactionDao = transactionDao;
    }

    public List<Budget> getMonthlyBudgetsWithSpending(Long userId, int month, int year) throws SQLException {
        List<Budget> budgets = budgetDao.findByUserAndMonth(userId, month, year);
        for (Budget budget : budgets) {
            BigDecimal spent = transactionDao.getCategorySpentInMonth(userId, budget.getCategoryId(), month, year);
            budget.setSpentAmount(spent);
        }
        return budgets;
    }

    public Budget saveBudget(Budget budget) throws SQLException {
        if (budget.getLimitAmount() == null || budget.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero.");
        }
        if (budget.getCategoryId() == null) {
            throw new IllegalArgumentException("A category must be selected for the budget.");
        }
        if (budget.getMonth() < 1 || budget.getMonth() > 12) {
            throw new IllegalArgumentException("Invalid month.");
        }
        if (budget.getYear() < 2000 || budget.getYear() > 2100) {
            throw new IllegalArgumentException("Invalid year.");
        }

        return budgetDao.saveOrUpdate(budget);
    }

    public boolean deleteBudget(Long id, Long userId) throws SQLException {
        return budgetDao.delete(id, userId);
    }
}

