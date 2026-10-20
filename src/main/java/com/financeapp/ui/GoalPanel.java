package com.financeapp.ui;

import com.financeapp.model.Goal;
import com.financeapp.model.GoalStatus;
import com.financeapp.service.GoalService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class GoalPanel extends JPanel {
    private final Long userId;
    private final Runnable onChangeCallback;
    private final GoalService goalService;

    private final JTable goalTable;
    private final DefaultTableModel tableModel;
    private List<Goal> currentGoals = new ArrayList<>();

    public GoalPanel(Long userId, Runnable onChangeCallback) {
        this.userId = userId;
        this.onChangeCallback = onChangeCallback;
        this.goalService = new GoalService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columns = {"ID", "Title", "Target ($)", "Saved ($)", "Deadline", "Status", "Progress"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        goalTable = new JTable(tableModel);
        goalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goalTable.setRowHeight(28);
        goalTable.getTableHeader().setFont(UITheme.FONT_BOLD);

        setupTableRenderers();

        JScrollPane scrollPane = new JScrollPane(goalTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton addBtn = new JButton("New Goal");
        JButton contributeBtn = new JButton("Add Contribution");
        JButton editBtn = new JButton("Edit Goal");
        JButton deleteBtn = new JButton("Delete Goal");

        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(e -> openGoalDialog(null));
        contributeBtn.addActionListener(e -> openContributionDialog());
        editBtn.addActionListener(e -> editSelectedGoal());
        deleteBtn.addActionListener(e -> deleteSelectedGoal());

        actionBar.add(addBtn);
        actionBar.add(contributeBtn);
        actionBar.add(editBtn);
        actionBar.add(deleteBtn);
        add(actionBar, BorderLayout.SOUTH);

        refreshData();
    }

    private void setupTableRenderers() {
        goalTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            private final JProgressBar progressBar = new JProgressBar(0, 100);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                int progress = value instanceof Number num ? num.intValue() : 0;
                progressBar.setValue(Math.min(100, Math.max(0, progress)));
                progressBar.setStringPainted(true);
                progressBar.setString(progress + "%");

                if (progress >= 100) {
                    progressBar.setForeground(UITheme.INCOME_COLOR);
                } else {
                    progressBar.setForeground(UITheme.PRIMARY);
                }

                return progressBar;
            }
        });
    }

    public void refreshData() {
        try {
            currentGoals = goalService.getGoals(userId);
            tableModel.setRowCount(0);
            for (Goal g : currentGoals) {
                int percentage = g.getProgressPercentage();
                tableModel.addRow(new Object[]{
                        g.getId(),
                        g.getTitle(),
                        String.format("%.2f", g.getTargetAmount()),
                        String.format("%.2f", g.getSavedAmount()),
                        g.getDeadline(),
                        g.getStatus(),
                        percentage
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load goals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openGoalDialog(Goal goal) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        GoalDialog dialog = new GoalDialog(parentFrame, userId, goal, goalService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            if (onChangeCallback != null) {
                onChangeCallback.run();
            }
        }
    }

    private void openContributionDialog() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an active goal to contribute to.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Goal selected = currentGoals.get(row);
        if (selected.getStatus() == GoalStatus.COMPLETED) {
            JOptionPane.showMessageDialog(this, "This goal is already marked as COMPLETED.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ContributionDialog dialog = new ContributionDialog(parentFrame, userId, selected, goalService);
        dialog.setVisible(true);
        if (dialog.isContributed()) {
            refreshData();
            if (onChangeCallback != null) {
                onChangeCallback.run();
            }
        }
    }

    private void editSelectedGoal() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a goal to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        openGoalDialog(currentGoals.get(row));
    }

    private void deleteSelectedGoal() {
        int row = goalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a goal to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Goal selected = currentGoals.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the goal '" + selected.getTitle() + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                goalService.deleteGoal(selected.getId(), userId);
                refreshData();
                if (onChangeCallback != null) {
                    onChangeCallback.run();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete goal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

