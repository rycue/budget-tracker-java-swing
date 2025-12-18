package budgettracker;

import javax.swing.*;
import java.awt.*;

public class GoalPanel extends JPanel {

    private Goal goal;
    private JLabel titleLabel;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JButton removeBtn;
    private JButton addBtn;
    private Runnable removeAction;

    public GoalPanel(Goal goal) {
        this.goal = goal;
        setLayout(new BorderLayout(5, 5));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        setPreferredSize(new Dimension(400, 120));
        setMinimumSize(new Dimension(400, 120));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Title
        titleLabel = new JLabel(goal.getName());
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Encouragement message
        messageLabel = new JLabel(getEncouragementMessage(), SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(getProgressPercent());
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 100));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        progressBar.setForeground(Color.decode("#00FF00")); // Bright green
        progressBar.setBackground(Color.decode("#2a2a2a")); // Dark background

        progressBar.setFont(new Font("Arial", Font.BOLD, 16));

        // Set progress bar text color to white
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.DARK_GRAY);

        // Remove button
        removeBtn = new JButton("Remove");
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.setBackground(Color.decode("#8B0000")); // Dark red
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);
        removeBtn.setFont(new Font("Arial", Font.BOLD, 12));
        removeBtn.setMaximumSize(new Dimension(100, 30));
        removeBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#A00000"), 2));
        removeBtn.addActionListener(e -> {
            Component parent = SwingUtilities.getWindowAncestor(this);
            if (removeAction != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        parent,
                        "Are you sure you want to remove the goal: " + goal.getName() + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    removeAction.run();
                }
            }
        });

        // Add to goal button
        addBtn = new JButton("+");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setBackground(Color.GREEN);
        addBtn.setForeground(Color.BLACK);
        addBtn.setFocusPainted(false);
        addBtn.setFont(new Font("Arial", Font.BOLD, 24));
        addBtn.setMaximumSize(new Dimension(100, 35));
        addBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#00AA00"), 2));
        addBtn.setToolTipText("Add to Goal");


        addBtn.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);

            // 1. Get the DashboardTab through the GoalsTab reference
            // We need to find the GoalsTab that contains this panel
            GoalsTab parentTab = (GoalsTab) SwingUtilities.getAncestorOfClass(GoalsTab.class, this);

            if (parentTab == null) {
                return;
            }

            // 2. Critical Check: How much cash do we actually have?
            double cashOnHand = parentTab.getDashboardTab().getCurrentTotalBalance();
            double remainingNeeded = goal.getTarget() - goal.getProgress();

            // 3. Informative Prompt
            String message = String.format(
                    "Goal: %s\n"
                    + "Available Cash: ₱%,.2f\n"
                    + "Remaining Needed: ₱%,.2f\n\n"
                    + "Enter amount to deposit:",
                    goal.getName(), cashOnHand, remainingNeeded);

            String input = JOptionPane.showInputDialog(parentWindow, message, "Deposit to Goal", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(input.trim());

                    // ENTERPRISE VALIDATION: Prevent Negative Balance
                    if (amount > cashOnHand) {
                        JOptionPane.showMessageDialog(parentWindow,
                                String.format("Insufficient Funds!\nYou only have ₱%,.2f available in your balance.", cashOnHand),
                                "Transaction Denied", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // EXISTING VALIDATION: Prevent Over-funding
                    if (amount > remainingNeeded) {
                        JOptionPane.showMessageDialog(parentWindow,
                                String.format("You only need ₱%,.2f to complete this goal.", remainingNeeded),
                                "Limit Exceeded", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(parentWindow, "Please enter a positive amount.");
                        return;
                    }

                    int userId = Integer.parseInt(AccountManager.getUserId());
                    if (DataHandler.fundGoal(userId, goal.getGoalID(), amount, goal.getName())) {
                        if (parentWindow instanceof BudgetTracker) {
                            ((BudgetTracker) parentWindow).refreshAllTabs();
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parentWindow, "Invalid number format.");
                }
            }
        });

        buttonPanel.add(removeBtn);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(addBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(messageLabel);
        centerPanel.add(progressBar);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        refresh();
    }

    public void setRemoveAction(Runnable action) {
        this.removeAction = action;
    }

    public void refresh() {
        double currentProgress = goal.getProgress();
        double targetAmount = goal.getTarget();

        progressBar.setValue(getProgressPercent());
        progressBar.setString(String.format("₱%.2f / ₱%.2f", currentProgress, targetAmount));
        messageLabel.setText(getEncouragementMessage());
        
        if (currentProgress >= targetAmount) {
            addBtn.setVisible(false);
        } else {
            addBtn.setVisible(true);
        }
    }

    private int getProgressPercent() {
        double percent = (goal.getProgress() / goal.getTarget()) * 100;
        return Math.min(100, (int) percent);
    }

    private String getEncouragementMessage() {
        double percent = (goal.getProgress() / goal.getTarget()) * 100;
        if (percent >= 100) {
            return "Goal achieved!";
        } else if (percent >= 75) {
            return "Almost there! Keep going!";
        } else if (percent >= 50) {
            return "You're halfway there!";
        } else if (percent > 0) {
            return "Good start, keep progressing!";
        } else {
            return "Start your journey!";
        }
    }
}
