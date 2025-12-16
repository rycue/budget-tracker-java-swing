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
            goals.add(goal);
            
            applyExistingTransactionsToGoal(goal);
            
            GoalPanel panel = new GoalPanel(goal);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            panel.setRemoveAction(() -> removeGoal(goal, panel));
            goalPanels.add(panel);
            goalsContainer.add(panel);
            goalsContainer.revalidate();
            goalsContainer.repaint();
        }
    }
    
    private void applyExistingTransactionsToGoal(Goal goal) {
        for (Transaction t : dashboardTab.getTransactions()) {
            goal.applyTransaction(t);
        }
    }

    private void removeGoal(Goal goal, GoalPanel panel) {
        goals.remove(goal);
        goalPanels.remove(panel);
        goalsContainer.remove(panel);
        goalsContainer.revalidate();
        goalsContainer.repaint();
    }

    public void applyTransactionToGoals(Transaction t) {
        for (int i = 0; i < goals.size(); i++) {
            goals.get(i).applyTransaction(t);
            goalPanels.get(i).refresh();
        }
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
    for (Goal goal : goals) {
        goal.reset();
    }
    
    for (Transaction t : dashboardTab.getTransactions()) {
        for (Goal goal : goals) {
            goal.applyTransaction(t);
        }
    }
    
    for (GoalPanel panel : goalPanels) {
        panel.refresh();
    }
}
}