package budgettracker;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class BudgetTracker extends JFrame {
    private JButton editToggleBtn;
    private DashboardTab dashboardTab;
    private GoalsTab goalsTab;
    private AnalyticsTab analyticsTab;
    private AccountTab accountTab;
    
    public DashboardTab getDashboardTab() {
        return this.dashboardTab;
    }

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
        
        // --- ONE SINGLE BOTTOM PANEL (Enterprise Best Practice) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(Color.BLACK);

        // 1. Initialize Toggle Button once
        editToggleBtn = new JButton("EDIT MODE: OFF");
        styleSecondaryButton(editToggleBtn);
        editToggleBtn.setVisible(true);

        // 2. Initialize Add Button once
        JButton addTransactionBtn = new JButton("+");
        addTransactionBtn.setFont(new Font("Arial", Font.BOLD, 38));
        addTransactionBtn.setPreferredSize(new Dimension(64, 48));
        styleButton(addTransactionBtn);

        // 3. Add to panel and then to frame
        bottomPanel.add(editToggleBtn);
        bottomPanel.add(addTransactionBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        add(tabPanel, BorderLayout.NORTH);     
        add(contentPanel, BorderLayout.CENTER);

        // --- ATTACH LISTENERS ONCE ---
        addTransactionBtn.addActionListener(e -> openTransactionDialog());

        editToggleBtn.addActionListener(e -> {
            dashboardTab.toggleEditMode();
            boolean active = editToggleBtn.getText().contains("OFF");
            editToggleBtn.setText(active ? "EDIT MODE: ON" : "EDIT MODE: OFF");
            editToggleBtn.setBackground(active ? Color.RED : Color.DARK_GRAY);
            editToggleBtn.setForeground(active ? Color.WHITE : Color.GREEN);
        });

        // Dashboard Switcher
        dashboardBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, "Dashboard");
            setActiveTab(dashboardBtn, goalsBtn, analyticsBtn, accountBtn);
            editToggleBtn.setVisible(true);
        });

        // Shared Switcher for other tabs to save space
        ActionListener tabResetListener = e -> {
            String cmd = e.getActionCommand(); // Make sure your buttons have ActionCommands set
            cardLayout.show(contentPanel, cmd);

            if (cmd.equals("Goals")) {
                setActiveTab(goalsBtn, dashboardBtn, analyticsBtn, accountBtn);
            } else if (cmd.equals("Analytics")) {
                setActiveTab(analyticsBtn, dashboardBtn, goalsBtn, accountBtn);
            } else if (cmd.equals("Account")) {
                setActiveTab(accountBtn, dashboardBtn, goalsBtn, analyticsBtn);
            }

            resetEditMode();
        };

        // You MUST set these commands for the shared listener to work
        goalsBtn.setActionCommand("Goals");
        analyticsBtn.setActionCommand("Analytics");
        accountBtn.setActionCommand("Account");

        goalsBtn.addActionListener(tabResetListener);
        analyticsBtn.addActionListener(tabResetListener);
        accountBtn.addActionListener(tabResetListener);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }
    
    private void openTransactionDialog() {
        TransactionDialog dialog = new TransactionDialog(this);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Transaction transaction = dialog.getTransaction();
            String userIdStr = AccountManager.getUserId();
            
            if (userIdStr != null) {
                int userId = Integer.parseInt(userIdStr);

                boolean success = DataHandler.saveToDatabase(transaction, userId);

                if (success) {
                    dashboardTab.addTransactionFromOutside(transaction);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save to database!");
                }
            }
        }
    }
    
    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
    
    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
    }
    
    private void resetEditMode() {
        editToggleBtn.setVisible(false);
        editToggleBtn.setText("EDIT MODE: OFF");
        editToggleBtn.setBackground(Color.DARK_GRAY);
        editToggleBtn.setForeground(Color.GREEN);
        dashboardTab.forceDisableEditMode();
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
    
    public static void showLogin() {
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        if (login.isSuccess()) {
            BudgetTracker mainApp = new BudgetTracker();

            // Load data immediately after login
            mainApp.dashboardTab.loadFromDatabase();
            String uid = AccountManager.getUserId();
            if (uid != null) {
                mainApp.goalsTab.loadExistingGoals(Integer.parseInt(uid));
            }

            mainApp.setVisible(true);
        } else {
            System.exit(0);
        }
    }
    
    public void refreshAllTabs() {
        // 1. Reload transactions into the Dashboard
        if (dashboardTab != null) {
            dashboardTab.loadFromDatabase();
        }

        // 2. Reload goals
        String uid = AccountManager.getUserId();
        if (uid != null && goalsTab != null) {
            goalsTab.loadExistingGoals(Integer.parseInt(uid));
        }

        // 3. Refresh the Account UI labels
        if (accountTab != null) {
            accountTab.refresh();
        }

        System.out.println("LOG: Global refresh complete. UI is now synced with Database.");
    }
    
    public static void main(String[] args) {
        SplashScreen splash = new SplashScreen(3000);
        splash.showSplash();

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isSuccess()) {
                BudgetTracker mainApp = new BudgetTracker();

                mainApp.dashboardTab.loadFromDatabase();

                String uid = AccountManager.getUserId();
                if (uid != null) {
                    mainApp.goalsTab.loadExistingGoals(Integer.parseInt(uid));
                }

                mainApp.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}