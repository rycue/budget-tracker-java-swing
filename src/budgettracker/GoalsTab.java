package budgettracker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GoalsTab extends JPanel {

    private DefaultListModel<String> goalsListModel;
    private ArrayList<Goal> goals = new ArrayList<>();

    private JButton addGoalBtn; // binago ko 'to nilagyan ko na nang ganito yung button ng addGoal para di na rin nakakalito
    private JButton editGoalBtn;
    private JButton deleteGoalBtn;

    private JList<String> goalsList;

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
        goalsList = new JList<>(goalsListModel);
        goalsList.setBackground(Color.BLACK);
        goalsList.setForeground(Color.GREEN);
        goalsList.setSelectionBackground(new Color(0, 100, 0));
        goalsList.setSelectionForeground(Color.WHITE);
        add(new JScrollPane(goalsList), BorderLayout.CENTER);

        // Buttons nilang tatlo
        addGoalBtn = new JButton("Add Goal");
        editGoalBtn = new JButton("Edit Goal");
        deleteGoalBtn = new JButton("Delete Goal");

        styleButton(addGoalBtn);
        styleButton(editGoalBtn);
        styleButton(deleteGoalBtn);

        // ito naka hide yung edit/delete button para hindi accedenteng mapindot ni user
        editGoalBtn.setEnabled(false);
        deleteGoalBtn.setEnabled(false);

        // Bottom panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.BLACK);
        btnPanel.add(addGoalBtn);
        btnPanel.add(editGoalBtn);
        btnPanel.add(deleteGoalBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // List selection listener → ito kita yung buttons pero hindi sya clickable hangga't walang pinipindot na row
        goalsList.addListSelectionListener(e -> {
            boolean hasSelection = !goalsList.isSelectionEmpty();
            editGoalBtn.setEnabled(hasSelection);
            deleteGoalBtn.setEnabled(hasSelection);
        });

        // Add Goal
        addGoalBtn.addActionListener(e -> openAddGoalDialog());

        // Edit Goal
        editGoalBtn.addActionListener(e -> openEditGoalDialog());

        // Delete Goal
        deleteGoalBtn.addActionListener(e -> deleteSelectedGoal());
    }
    
    // lagyan ko pagkakalinlan nila para madali i-edit kapag may error o gusto baguhin
    // ADD GOAL
    private void openAddGoalDialog() {
        GoalDialog dialog = new GoalDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Goal goal = dialog.getGoal();
            goals.add(goal);
            goalsListModel.addElement(goal.toString());
        }
    }

    // EDIT GOAL
    private void openEditGoalDialog() {
        int index = goalsList.getSelectedIndex();
        if (index == -1) return;

        Goal selectedGoal = goals.get(index);

        GoalDialog dialog = new GoalDialog(SwingUtilities.getWindowAncestor(this), selectedGoal);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Goal updatedGoal = dialog.getGoal();
            goals.set(index, updatedGoal);
            goalsListModel.set(index, updatedGoal.toString());
        }
    }

    // DELETE GOAL
    private void deleteSelectedGoal() {
        int index = goalsList.getSelectedIndex();
        if (index == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this goal?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            goals.remove(index);
            goalsListModel.remove(index);
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
}