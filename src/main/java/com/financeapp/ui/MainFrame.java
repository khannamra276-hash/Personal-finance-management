package com.financeapp.ui;

import com.financeapp.model.User;
import com.financeapp.util.Session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    private final User currentUser;
    private final JTabbedPane tabbedPane;
    private final DashboardPanel dashboardPanel;
    private final TransactionPanel transactionPanel;
    private final BudgetPanel budgetPanel;
    private final GoalPanel goalPanel;

    public MainFrame(User user) {
        super("Online Personal Finance Management Platform");
        this.currentUser = user;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1120, 740);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);

        dashboardPanel = new DashboardPanel(currentUser.getId());
        transactionPanel = new TransactionPanel(currentUser.getId(), this::onTransactionChanged);
        budgetPanel = new BudgetPanel(currentUser.getId(), this::onBudgetChanged);
        goalPanel = new GoalPanel(currentUser.getId(), this::onGoalChanged);

        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Transactions", transactionPanel);
        tabbedPane.addTab("Monthly Budgets", budgetPanel);
        tabbedPane.addTab("Savings Goals", goalPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                dashboardPanel.refreshData();
            } else if (selectedIndex == 1) {
                transactionPanel.refreshData();
            } else if (selectedIndex == 2) {
                budgetPanel.refreshData();
            } else if (selectedIndex == 3) {
                goalPanel.refreshData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel titleLabel = new JLabel("Online Personal Finance Platform");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.PRIMARY);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userBadge = new JLabel("Signed in as: " + currentUser.getName());
        userBadge.setFont(UITheme.FONT_REGULAR);
        userBadge.setForeground(UITheme.TEXT_MUTED);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.addActionListener(e -> handleLogout());

        userPanel.add(userBadge);
        userPanel.add(logoutBtn);

        bar.add(titleLabel, BorderLayout.WEST);
        bar.add(userPanel, BorderLayout.EAST);
        return bar;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            Session.clear();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void onTransactionChanged() {
        dashboardPanel.refreshData();
        budgetPanel.refreshData();
    }

    private void onBudgetChanged() {
        dashboardPanel.refreshData();
    }

    private void onGoalChanged() {
        dashboardPanel.refreshData();
    }
}

