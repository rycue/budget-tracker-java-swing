package budgettracker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GoalsTab extends JPanel {

    private ArrayList<Goal> goals = new ArrayList<>();
    private ArrayList<GoalPanel> goalPanels = new ArrayList<>();
    private JPanel goalsContainer;
    private DashboardTab dashboardTab;

    public GoalsTab(DashboardTab dashboardTab) {
        this.dashboardTab = dashboardTab;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);
        initUI();
    }

    private void initUI() {
        JLabel title = new JLabel("GOALS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.GREEN);
        add(title, BorderLayout.NORTH);

        goalsContainer = new JPanel();
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBackground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(goalsContainer);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JButton addGoalBtn = new JButton("Add Goal");
        addGoalBtn.setFont(new Font("Arial", Font.BOLD, 24));
        addGoalBtn.setPreferredSize(new Dimension(150, 50));
        styleButton(addGoalBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.BLACK);
        btnPanel.add(addGoalBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addGoalBtn.addActionListener(e -> openGoalDialog());
    }

    private void openGoalDialog() {
        if (goals.size() >= 10) {
            JOptionPane.showMessageDialog(this, "You can only add up to 10 goals.");
            return;
        }

        GoalDialog dialog = new GoalDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Goal goal = dialog.getGoal();
            UserAccount user = AccountManager.getUser(); // Get current logged-in user

            // SAVE TO DATABASE
            int newId = DataHandler.saveGoal(goal, user.getUserID());

            if (newId != -1) {
                goal.setGoalID(newId); // Set the ID returned from databse
                goals.add(goal);

                GoalPanel panel = new GoalPanel(goal);
                panel.refresh(); // Ensure ₱ amounts show immediately
                panel.setRemoveAction(() -> removeGoal(goal, panel));

                goalsContainer.add(panel);
                goalsContainer.revalidate();
                goalsContainer.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save goal to database.");
            }
        }
    }

    private void removeGoal(Goal goal, GoalPanel panel) {
        String[] options = {"Refund to Balance", "Permanent Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "This goal has ₱" + String.format("%.2f", goal.getProgress()) + " saved. What should we do?",
                "Delete Goal",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return; // Exit if Cancel
        }
        boolean refund = (choice == 0);
        int userId = Integer.parseInt(AccountManager.getUserId());

        if (DataHandler.deleteGoal(goal.getGoalID(), userId, refund, goal.getProgress())) {
            // Refresh all tabs so the balance and goal list update instantly
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof BudgetTracker) {
                ((BudgetTracker) window).refreshAllTabs();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error: Could not delete goal.");
        }
    }
    
    public void loadExistingGoals(int userId) {
        goals.clear();
        goalPanels.clear();
        goalsContainer.removeAll();

        // Fetch from Database
        ArrayList<Goal> loadedGoals = DataHandler.loadGoals(userId);

        // Rebuild the UI panels
        for (Goal g : loadedGoals) {
            goals.add(g);
            GoalPanel panel = new GoalPanel(g);

            // This is crucial: link the remove logic
            panel.setRemoveAction(() -> removeGoal(g, panel));

            goalPanels.add(panel);
            goalsContainer.add(panel);
        }

        // Refresh UI
        goalsContainer.revalidate();
        goalsContainer.repaint();
    }

    public void applyTransactionToGoals(Transaction t) {
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public int getGoalCount() {
        return goals.size();
    }

    public void recalculateAllGoals() {
    }
}
