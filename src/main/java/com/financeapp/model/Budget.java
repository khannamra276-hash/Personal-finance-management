package com.financeapp.model;

import java.math.BigDecimal;

public class Budget {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private int month;
    private int year;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount = BigDecimal.ZERO;

    public Budget() {
    }

    public Budget(Long id, Long userId, Long categoryId, String categoryName,
                  int month, int year, BigDecimal limitAmount) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.month = month;
        this.year = year;
        this.limitAmount = limitAmount;
    }

    public Budget(Long userId, Long categoryId, int month, int year, BigDecimal limitAmount) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.month = month;
        this.year = year;
        this.limitAmount = limitAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }
}

