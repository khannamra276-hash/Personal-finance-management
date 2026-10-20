package com.financeapp.service;

import java.math.BigDecimal;

public record BudgetWarning(
        boolean exceededLimit,
        boolean exceededWarningThreshold,
        BigDecimal limitAmount,
        BigDecimal totalSpent,
        BigDecimal percentage,
        String categoryName
) {
    public String getFormattedMessage() {
        if (exceededLimit) {
            return String.format("Budget Exceeded! Spending for '%s' is now at %s%% (Limit: $%s, Spent: $%s).",
                    categoryName, percentage.toPlainString(), limitAmount.toPlainString(), totalSpent.toPlainString());
        } else if (exceededWarningThreshold) {
            return String.format("Budget Warning: Spending for '%s' has reached %s%% of its monthly limit (Limit: $%s, Spent: $%s).",
                    categoryName, percentage.toPlainString(), limitAmount.toPlainString(), totalSpent.toPlainString());
        }
        return "";
    }
}

