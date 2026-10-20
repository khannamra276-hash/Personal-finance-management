package com.financeapp.ui;

import com.financeapp.model.Budget;
import com.financeapp.model.Goal;
import com.financeapp.service.DashboardService;
import com.financeapp.service.DashboardSummary;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class DashboardPanel extends JPanel {
    private final Long userId;
    private final DashboardService dashboardService;

    private final JComboBox<Integer> monthCombo;
    private final JTextField yearField;

    private final JLabel incomeValueLbl;
    private final JLabel expenseValueLbl;
    private final JLabel netBalanceValueLbl;

    private final JPanel pieChartContainer;
    private final JPanel barChartContainer;
    private final JPanel goalsContainer;

    public DashboardPanel(Long userId) {
        this.userId = userId;
        this.dashboardService = new DashboardService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topBar.setBorder(UITheme.createCardBorder());

        topBar.add(new JLabel("Dashboard Period - Month:"));
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
        topBar.add(monthCombo);

        topBar.add(new JLabel("Year:"));
        yearField = new JTextField(String.valueOf(LocalDate.now().getYear()), 6);
        topBar.add(yearField);

        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setBackground(UITheme.PRIMARY);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> refreshData());
        topBar.add(refreshBtn);

        add(topBar, BorderLayout.NORTH);

        // Main Scrollable Dashboard Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Metric Summary Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        incomeValueLbl = new JLabel("$0.00");
        expenseValueLbl = new JLabel("$0.00");
        netBalanceValueLbl = new JLabel("$0.00");

        cardsPanel.add(createMetricCard("TOTAL INCOME", incomeValueLbl, UITheme.INCOME_COLOR));
        cardsPanel.add(createMetricCard("TOTAL EXPENSES", expenseValueLbl, UITheme.EXPENSE_COLOR));
        cardsPanel.add(createMetricCard("NET BALANCE", netBalanceValueLbl, UITheme.PRIMARY));

        contentPanel.add(cardsPanel);
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Charts Row (Pie Chart and Bar Chart side by side)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsPanel.setPreferredSize(new Dimension(800, 320));
        chartsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        pieChartContainer = new JPanel(new BorderLayout());
        pieChartContainer.setBorder(UITheme.createCardBorder());
        pieChartContainer.setBackground(Color.WHITE);

        barChartContainer = new JPanel(new BorderLayout());
        barChartContainer.setBorder(UITheme.createCardBorder());
        barChartContainer.setBackground(Color.WHITE);

        chartsPanel.add(pieChartContainer);
        chartsPanel.add(barChartContainer);

        contentPanel.add(chartsPanel);
        contentPanel.add(Box.createVerticalStrut(14));

        // 3. Goals Progress Overview Section
        JPanel goalsHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JLabel goalsTitle = new JLabel("Active Savings Goals Progress");
        goalsTitle.setFont(UITheme.FONT_SUBHEADER);
        goalsHeader.add(goalsTitle);
        contentPanel.add(goalsHeader);
        contentPanel.add(Box.createVerticalStrut(6));

        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBorder(UITheme.createCardBorder());
        goalsContainer.setBackground(Color.WHITE);

        contentPanel.add(goalsContainer);

        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        int month = (Integer) monthCombo.getSelectedItem();
        int year;
        try {
            year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            year = LocalDate.now().getYear();
            yearField.setText(String.valueOf(year));
        }

        try {
            DashboardSummary summary = dashboardService.getSummary(userId, month, year);
            updateMetricCards(summary);
            updatePieChart(summary.expenseByCategory());
            updateBarChart(summary.budgets());
            updateGoalsList(summary.activeGoals());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMetricCards(DashboardSummary summary) {
        incomeValueLbl.setText(String.format("$%.2f", summary.totalIncome()));
        expenseValueLbl.setText(String.format("$%.2f", summary.totalExpenses()));
        netBalanceValueLbl.setText(String.format("$%.2f", summary.netBalance()));
        if (summary.netBalance().compareTo(BigDecimal.ZERO) < 0) {
            netBalanceValueLbl.setForeground(UITheme.EXPENSE_COLOR);
        } else {
            netBalanceValueLbl.setForeground(UITheme.PRIMARY);
        }
    }

    private void updatePieChart(Map<String, BigDecimal> expenseMap) {
        pieChartContainer.removeAll();
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        for (Map.Entry<String, BigDecimal> entry : expenseMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(entry.getKey(), entry.getValue().doubleValue());
            }
        }

        if (dataset.getItemCount() == 0) {
            JLabel emptyLabel = new JLabel("No expense data recorded for this month.", JLabel.CENTER);
            emptyLabel.setFont(UITheme.FONT_REGULAR);
            emptyLabel.setForeground(UITheme.TEXT_MUTED);
            pieChartContainer.add(emptyLabel, BorderLayout.CENTER);
        } else {
            JFreeChart pieChart = ChartFactory.createPieChart(
                    "Expenses by Category",
                    dataset,
                    true,
                    true,
                    false
            );
            pieChart.setBackgroundPaint(Color.WHITE);
            PiePlot<?> plot = (PiePlot<?>) pieChart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setSimpleLabels(true);

            ChartPanel chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new Dimension(380, 260));
            chartPanel.setBackground(Color.WHITE);
            pieChartContainer.add(chartPanel, BorderLayout.CENTER);
        }

        pieChartContainer.revalidate();
        pieChartContainer.repaint();
    }

    private void updateBarChart(java.util.List<Budget> budgets) {
        barChartContainer.removeAll();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Budget b : budgets) {
            dataset.addValue(b.getLimitAmount().doubleValue(), "Budget Limit", b.getCategoryName());
            dataset.addValue(b.getSpentAmount().doubleValue(), "Actual Spent", b.getCategoryName());
        }

        if (budgets.isEmpty()) {
            JLabel emptyLabel = new JLabel("No budgets configured for this month.", JLabel.CENTER);
            emptyLabel.setFont(UITheme.FONT_REGULAR);
            emptyLabel.setForeground(UITheme.TEXT_MUTED);
            barChartContainer.add(emptyLabel, BorderLayout.CENTER);
        } else {
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Budget vs Actual Spending",
                    "Category",
                    "Amount ($)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            barChart.setBackgroundPaint(Color.WHITE);
            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(250, 250, 250));
            plot.setOutlineVisible(false);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, UITheme.PRIMARY);
            renderer.setSeriesPaint(1, UITheme.WARNING_COLOR);

            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new Dimension(380, 260));
            chartPanel.setBackground(Color.WHITE);
            barChartContainer.add(chartPanel, BorderLayout.CENTER);
        }

        barChartContainer.revalidate();
        barChartContainer.repaint();
    }

    private void updateGoalsList(java.util.List<Goal> goals) {
        goalsContainer.removeAll();

        if (goals.isEmpty()) {
            JLabel emptyLbl = new JLabel("No active savings goals. Add one in the Savings Goals tab!");
            emptyLbl.setFont(UITheme.FONT_REGULAR);
            emptyLbl.setForeground(UITheme.TEXT_MUTED);
            emptyLbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            goalsContainer.add(emptyLbl);
        } else {
            for (Goal g : goals) {
                JPanel goalRow = new JPanel(new BorderLayout(10, 4));
                goalRow.setOpaque(false);
                goalRow.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

                String infoText = String.format("%s (Target: $%.2f, Saved: $%.2f, Deadline: %s)",
                        g.getTitle(), g.getTargetAmount(), g.getSavedAmount(), g.getDeadline());
                JLabel titleLbl = new JLabel(infoText);
                titleLbl.setFont(UITheme.FONT_REGULAR);

                int progress = g.getProgressPercentage();
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(progress);
                bar.setStringPainted(true);
                bar.setString(progress + "%");
                bar.setPreferredSize(new Dimension(180, 20));
                bar.setForeground(progress >= 100 ? UITheme.INCOME_COLOR : UITheme.PRIMARY);

                goalRow.add(titleLbl, BorderLayout.WEST);
                goalRow.add(bar, BorderLayout.EAST);
                goalsContainer.add(goalRow);
            }
        }

        goalsContainer.revalidate();
        goalsContainer.repaint();
    }
}

