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
            if (removeAction != null) {
                removeAction.run();
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
            String input = JOptionPane.showInputDialog(
                    this,
                    "Enter amount to add to \"" + goal.getName() + "\":",
                    "Add to Goal",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (input != null && !input.trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(input.trim());

                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Amount must be greater than zero.",
                                "Invalid Amount",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    if (amount > 1000000000) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Amount is too large. Please enter a reasonable amount.",
                                "Invalid Amount",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    String amountStr = input.trim();
                    if (amountStr.contains(".")) {
                        String[] parts = amountStr.split("\\.");
                        if (parts.length > 1 && parts[1].length() > 2) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Amount can have at most 2 decimal places.\nExample: 100.50",
                                    "Invalid Amount",
                                    JOptionPane.ERROR_MESSAGE
                            );
                            return;
                        }
                    }

                    goal.addManualProgress(amount);
                    refresh();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a valid number.\nExample: 100 or 100.50",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE
                    );
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
    }

    public void setRemoveAction(Runnable action) {
        this.removeAction = action;
    }

    public void refresh() {
        progressBar.setValue(getProgressPercent());
        progressBar.setString(String.format("₱%.2f / ₱%.2f", goal.getProgress(), goal.getTarget()));
        messageLabel.setText(getEncouragementMessage());
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
