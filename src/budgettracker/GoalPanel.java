package budgettracker;
import javax.swing.*;
import java.awt.*;

public class GoalPanel extends JPanel {
    private Goal goal;
    private JLabel titleLabel;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JButton removeBtn;
    private Runnable removeAction;

    public GoalPanel(Goal goal) {
        this.goal = goal;
        setLayout(new BorderLayout(5, 5)); // smaller spacing
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

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
        UIManager.put("ProgressBar.foreground", Color.BLACK);
        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
        UIManager.put("ProgressBar.selectionBackground", Color.BLACK);

        // Remove button
        removeBtn = new JButton("Remove");
        removeBtn.setBackground(Color.WHITE); // White background
        removeBtn.setForeground(Color.BLACK); // Black text
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> {
            if (removeAction != null) removeAction.run();
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(removeBtn, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(messageLabel);
        centerPanel.add(progressBar);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setRemoveAction(Runnable action) {
        this.removeAction = action;
    }

    public void refresh() {
        progressBar.setValue(getProgressPercent());
        messageLabel.setText(getEncouragementMessage());
    }

    private int getProgressPercent() {
        double percent = (goal.getProgress() / goal.getTarget()) * 100;
        return Math.min(100, (int) percent);
    }

    private String getEncouragementMessage() {
        double percent = (goal.getProgress() / goal.getTarget()) * 100;
        if (percent >= 100) return "Goal achieved! 🎉";
        else if (percent >= 75) return "Almost there! Keep going!";
        else if (percent >= 50) return "You're halfway there!";
        else if (percent > 0) return "Good start, keep progressing!";
        else return "Start your journey!";
    }
}