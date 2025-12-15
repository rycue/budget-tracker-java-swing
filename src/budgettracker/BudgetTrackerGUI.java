package budgettracker;

import javax.swing.*;
import java.awt.*;

public class BudgetTrackerGUI extends JFrame {

    private DashboardTab dashboardTab;
    private GoalsTab goalsTab;
    private AnalyticsTab analyticsTab;
    private AccountTab accountTab;

    public BudgetTrackerGUI() {
        setTitle("Budget Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // TAB PANE
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.BLACK);
        tabbedPane.setForeground(Color.GREEN);

        // CREATE TABS
        dashboardTab = new DashboardTab();
        analyticsTab = new AnalyticsTab(dashboardTab);
        dashboardTab.setAnalyticsTab(analyticsTab);
        goalsTab = new GoalsTab(dashboardTab);
        accountTab = new AccountTab();

        tabbedPane.addTab("Dashboard", dashboardTab);
        tabbedPane.addTab("Goals", goalsTab);
        tabbedPane.addTab("Analytics", analyticsTab);
        tabbedPane.addTab("Account", accountTab);

        add(tabbedPane, BorderLayout.CENTER);

        // GLOBAL ADD TRANSACTION BUTTON
        JButton addTransactionBtn = new JButton("Add Transaction");
        styleButton(addTransactionBtn);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(addTransactionBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        addTransactionBtn.addActionListener(e -> openTransactionDialog());

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }

    private void openTransactionDialog() {
        TransactionDialog dialog = new TransactionDialog(this);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Transaction t = dialog.getTransaction();

            // Update dashboard
            dashboardTab.addTransactionFromOutside(t);

            // Update goals automatically
            goalsTab.applyTransactionToGoals(t);
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    // Getter for AnalyticsTab
    public AnalyticsTab getAnalyticsTab() {
        return analyticsTab;
    }

    // ✅ UPDATED MAIN METHOD (LOGIN FIRST)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Show Login Dialog first
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            // Open app only if login succeeds
            if (login.isSuccess()) {
                new BudgetTrackerGUI().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
