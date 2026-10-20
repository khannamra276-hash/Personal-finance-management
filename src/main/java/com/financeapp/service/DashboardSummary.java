package com.financeapp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.financeapp.model.Budget;
import com.financeapp.model.Goal;

public record DashboardSummary(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        Map<String, BigDecimal> expenseByCategory,
        List<Budget> budgets,
        List<Goal> activeGoals
) {}

