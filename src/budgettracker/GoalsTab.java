package budgettracker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GoalsTab extends JPanel {

    private DefaultListModel<String> goalsListModel;
    private ArrayList<Goal> goals = new ArrayList<>();

    public GoalsTab() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);
        initUI();
    }

    private void initUI() {
        // Title
        JLabel title = new JLabel("Your Goals", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.GREEN);
        add(title, BorderLayout.NORTH);

        // List of goals
        goalsListModel = new DefaultListModel<>();
        JList<String> goalsList = new JList<>(goalsListModel);
        goalsList.setBackground(Color.BLACK);
        goalsList.setForeground(Color.GREEN);
        goalsList.setSelectionBackground(new Color(0, 100, 0));
        goalsList.setSelectionForeground(Color.WHITE);
        add(new JScrollPane(goalsList), BorderLayout.CENTER);

        JButton addGoalBtn = new JButton("Add Goal");
        styleButton(addGoalBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.BLACK);
        btnPanel.add(addGoalBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addGoalBtn.addActionListener(e -> openGoalDialog());
    }

    private void openGoalDialog() {
        GoalDialog dialog = new GoalDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Goal goal = dialog.getGoal();
            goals.add(goal);
            goalsListModel.addElement(goal.toString());
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
}
