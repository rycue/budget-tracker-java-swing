package budgettracker;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class BudgetTracker extends JFrame {
    private DashboardTab dashboardTab;
    private GoalsTab goalsTab;
    private AnalyticsTab analyticsTab;
    private AccountTab accountTab;

    public BudgetTracker() {
        setTitle("Budget Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);
        setResizable(false);
        
        // CREATE TABS
        dashboardTab = new DashboardTab();
        analyticsTab = new AnalyticsTab(dashboardTab);
        goalsTab = new GoalsTab(dashboardTab);
        accountTab = new AccountTab();
       
        dashboardTab.setAnalyticsTab(analyticsTab);
        dashboardTab.setGoalsTab(goalsTab); 
        
        JPanel tabPanel = new JPanel(new GridLayout(1, 4, 0, 0)); 
        tabPanel.setBackground(Color.BLACK);
        
        JButton dashboardBtn = createTabButton("Dashboard", true);
        JButton goalsBtn = createTabButton("Goals", false);
        JButton analyticsBtn = createTabButton("Analytics", false);
        JButton accountBtn = createTabButton("Account", false);
        
        tabPanel.add(dashboardBtn);
        tabPanel.add(goalsBtn);
        tabPanel.add(analyticsBtn);
        tabPanel.add(accountBtn);
        
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.add(dashboardTab, "Dashboard");
        contentPanel.add(goalsTab, "Goals");
        contentPanel.add(analyticsTab, "Analytics");
        contentPanel.add(accountTab, "Account");
        
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        
        dashboardBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Dashboard");
            setActiveTab(dashboardBtn, goalsBtn, analyticsBtn, accountBtn);
        });
        
        goalsBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Goals");
            setActiveTab(goalsBtn, dashboardBtn, analyticsBtn, accountBtn);
        });
        
        analyticsBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Analytics");
            setActiveTab(analyticsBtn, dashboardBtn, goalsBtn, accountBtn);
        });
        
        accountBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Account");
            setActiveTab(accountBtn, dashboardBtn, goalsBtn, analyticsBtn);
        });
        
        add(tabPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // GLOBAL ADD TRANSACTION BUTTON
        JButton addTransactionBtn = new JButton("+");
        addTransactionBtn.setFont(new Font("Arial", Font.BOLD, 38));
        addTransactionBtn.setPreferredSize(new Dimension(64, 48));
        addTransactionBtn.setToolTipText("Add Transaction");
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
    
    private JButton createTabButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1)); 
        btn.setPreferredSize(new Dimension(0, 36));
        
        if (isActive) {
            btn.setBackground(Color.GREEN);
            btn.setForeground(Color.BLACK);
        } else {
            btn.setBackground(Color.decode("#1b1b1b"));
            btn.setForeground(Color.GREEN);
        }
        
        return btn;
    }
    
    private void setActiveTab(JButton active, JButton... others) {
        active.setBackground(Color.GREEN);
        active.setForeground(Color.BLACK);
        
        for (JButton btn : others) {
            btn.setBackground(Color.decode("#1b1b1b"));
            btn.setForeground(Color.GREEN);
        }
    }
    
    public AnalyticsTab getAnalyticsTab() {
        return analyticsTab;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isSuccess()) {
                new BudgetTracker().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}