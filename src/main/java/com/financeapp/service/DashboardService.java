package com.financeapp.service;

import com.financeapp.dao.GoalDao;
import com.financeapp.dao.TransactionDao;
import com.financeapp.model.Budget;
import com.financeapp.model.CategoryType;
import com.financeapp.model.Goal;
import com.financeapp.model.GoalStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final TransactionDao transactionDao;
    private final BudgetService budgetService;
    private final GoalDao goalDao;

    public DashboardService() {
        this.transactionDao = new TransactionDao();
        this.budgetService = new BudgetService();
        this.goalDao = new GoalDao();
    }

    public DashboardService(TransactionDao transactionDao, BudgetService budgetService, GoalDao goalDao) {
        this.transactionDao = transactionDao;
        this.budgetService = budgetService;
        this.goalDao = goalDao;
    }

    public DashboardSummary getSummary(Long userId, int month, int year) throws SQLException {
        BigDecimal totalIncome = transactionDao.getTotalAmountByTypeAndMonth(userId, CategoryType.INCOME, month, year);
        BigDecimal totalExpenses = transactionDao.getTotalAmountByTypeAndMonth(userId, CategoryType.EXPENSE, month, year);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> expenseByCategory = transactionDao.getExpenseBreakdownByCategory(userId, month, year);
        List<Budget> budgets = budgetService.getMonthlyBudgetsWithSpending(userId, month, year);
        List<Goal> activeGoals = goalDao.findByUserId(userId).stream()
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .toList();

        return new DashboardSummary(
                totalIncome,
                totalExpenses,
                netBalance,
                expenseByCategory,
                budgets,
                activeGoals
        );
    }
}

