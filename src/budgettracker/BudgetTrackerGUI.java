package budgettracker;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatDarculaLaf;

public class BudgetTrackerGUI extends JFrame {

    private DashboardTab dashboardTab;
    private GoalsTab goalsTab;
    private AnalyticsTab analyticsTab;
    private AccountTab accountTab;

    public BudgetTrackerGUI() {
        setTitle("Budget Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK); // background

        // Tabs 
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.BLACK);
        tabbedPane.setForeground(Color.GREEN);

        dashboardTab = new DashboardTab();
        goalsTab = new GoalsTab();
        analyticsTab = new AnalyticsTab(dashboardTab);
        accountTab = new AccountTab();

        tabbedPane.addTab("Dashboard", dashboardTab);
        tabbedPane.addTab("Goals", goalsTab);
        tabbedPane.addTab("Analytics", analyticsTab);
        tabbedPane.addTab("Account", accountTab);

        add(tabbedPane, BorderLayout.CENTER);

        // GLOBAL Add Transaction button 
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
            dashboardTab.addTransactionFromOutside(t);
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            FlatDarculaLaf.setup();

            if (AccountManager.getUser() == null) {
                RegisterDialog register = new RegisterDialog(null);
                register.setVisible(true);
            }

            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isSuccess()) {
                new BudgetTrackerGUI().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
