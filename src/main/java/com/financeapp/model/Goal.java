package com.financeapp.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Goal {
    private Long id;
    private Long userId;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private LocalDate deadline;
    private GoalStatus status;
    private LocalDateTime createdAt;

    public Goal() {
        this.savedAmount = BigDecimal.ZERO;
        this.status = GoalStatus.IN_PROGRESS;
    }

    public Goal(Long id, Long userId, String title, BigDecimal targetAmount,
                BigDecimal savedAmount, LocalDate deadline, GoalStatus status,
                LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Goal(Long userId, String title, BigDecimal targetAmount,
                BigDecimal savedAmount, LocalDate deadline, GoalStatus status) {
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        this.deadline = deadline;
        this.status = status;
    }

    public int getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal current = savedAmount != null ? savedAmount : BigDecimal.ZERO;
        BigDecimal ratio = current.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(100, ratio.intValue());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

