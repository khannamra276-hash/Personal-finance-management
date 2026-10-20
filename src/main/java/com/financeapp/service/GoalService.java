package com.financeapp.service;

import com.financeapp.dao.GoalDao;
import com.financeapp.model.Goal;
import com.financeapp.model.GoalStatus;
import com.financeapp.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class GoalService {
    private final GoalDao goalDao;

    public GoalService() {
        this.goalDao = new GoalDao();
    }

    public GoalService(GoalDao goalDao) {
        this.goalDao = goalDao;
    }

    public List<Goal> getGoals(Long userId) throws SQLException {
        return goalDao.findByUserId(userId);
    }

    public Goal createGoal(Goal goal) throws SQLException {
        validateGoal(goal);
        return goalDao.create(goal);
    }

    public boolean updateGoal(Goal goal) throws SQLException {
        validateGoal(goal);
        if (goal.getSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        return goalDao.update(goal);
    }

    public boolean addContribution(Long goalId, Long userId, BigDecimal contribution) throws SQLException {
        if (contribution == null || contribution.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero.");
        }
        return goalDao.addContribution(goalId, userId, contribution);
    }

    public boolean deleteGoal(Long id, Long userId) throws SQLException {
        return goalDao.delete(id, userId);
    }

    private void validateGoal(Goal goal) {
        if (!InputValidator.isNotEmpty(goal.getTitle())) {
            throw new IllegalArgumentException("Goal title cannot be empty.");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }
        if (goal.getSavedAmount() == null || goal.getSavedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saved amount cannot be negative.");
        }
        if (goal.getDeadline() == null) {
            throw new IllegalArgumentException("Target deadline date is required.");
        }
    }
}

