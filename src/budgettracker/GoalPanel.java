package budgettracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;

public class GoalPanel extends JPanel {

    private Goal goal;
    private JLabel titleLabel, messageLabel, dateLabel, currentLabel, targetLabel;
    private JButton removeBtn, addBtn;
    private Runnable removeAction;

    private final Color DASH_GREEN = new Color(0, 255, 0);
    private final Color GLOW_DARK = new Color(0, 180, 0);
    private final Color HUD_BG = new Color(0, 12, 0, 225);

    public GoalPanel(Goal goal) {
        this.goal = goal;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBorder(new EmptyBorder(10, 20, 15, 20));

        setPreferredSize(new Dimension(500, 160));
        setMinimumSize(new Dimension(450, 160));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        initUI();
    }

    private String getAgeStatus() {
        LocalDate start = goal.getDateCreated();
        LocalDate today = LocalDate.now();

        if (start == null) {
            return "";
        }

        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(start, today);
        String dateStr = start.toString();

        // Fix for future dates in DB: treat as Today if daysDiff is 0 or less
        if (daysDiff <= 0) {
            return "since " + dateStr + " (Today)";
        } else if (daysDiff == 1) {
            return "since " + dateStr + " (Yesterday)";
        } else {
            return "since " + dateStr + " (" + daysDiff + " days ago)";
        }
    }

    private void initUI() {
        // --- ROW 1: TITLE AND DATA READOUT ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleLabel = new JLabel(goal.getName().toUpperCase());
        titleLabel.setForeground(DASH_GREEN);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));

        JPanel dataHud = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        dataHud.setOpaque(false);
        dataHud.add(createValueDisplay("TARGET", targetLabel = new JLabel(), 18, false));

        currentLabel = new JLabel();
        currentLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        dataHud.add(createValueDisplay("CURRENT", currentLabel, 22, true));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(dataHud, BorderLayout.EAST);

        // --- NEW ROW: DATE INDICATOR (Below Title) ---
        dateLabel = new JLabel();
        dateLabel.setForeground(new Color(150, 200, 150));
        dateLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setBorder(new EmptyBorder(2, 5, 0, 0));

        // --- ROW 2: MESSAGE AND ACTION BUTTONS ---
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setBorder(new EmptyBorder(5, 0, 5, 0));

        messageLabel = new JLabel();
        messageLabel.setForeground(new Color(180, 255, 180));
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 13));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);
        addBtn = createActionButton("+", DASH_GREEN, Color.BLACK);
        removeBtn = createActionButton("X", new Color(180, 0, 0), Color.WHITE);
        addBtn.addActionListener(e -> handleDeposit());
        removeBtn.addActionListener(e -> {
            if (removeAction != null) {
                removeAction.run();
            }
        });

        btnGroup.add(addBtn);
        btnGroup.add(removeBtn);

        actionRow.add(messageLabel, BorderLayout.WEST);
        actionRow.add(btnGroup, BorderLayout.EAST);

        // STACKING THE COMPONENTS
        add(header);
        add(dateLabel); // The indicator you requested
        add(Box.createVerticalStrut(5));
        add(actionRow);
        add(Box.createVerticalGlue());

        refresh();
    }

    public void refresh() {
        targetLabel.setText(String.format("%.2f", goal.getTarget()));
        currentLabel.setText(String.format("%.2f", goal.getProgress()));

        // Update both text fields separately
        dateLabel.setText(getAgeStatus());
        messageLabel.setText("> " + getEncouragementMessage());

        addBtn.setVisible(goal.getProgress() < goal.getTarget());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(HUD_BG);
        g2.fill(new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, 12, 12));
        g2.setColor(GLOW_DARK);
        g2.draw(new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, 12, 12));

        int barHeight = 22;
        int barY = getHeight() - 32;
        int barW = getWidth() - 40;
        int progW = (int) (barW * (getProgressPercent() / 100.0));

        g2.setColor(new Color(0, 20, 0));
        g2.fillRect(20, barY, barW, barHeight);

        GradientPaint gradient = new GradientPaint(20, 0, GLOW_DARK, 20 + progW, 0, DASH_GREEN);
        g2.setPaint(gradient);
        for (int i = 0; i < progW; i += 10) {
            g2.fillRect(20 + i, barY, 8, barHeight);
        }

        String percentText = getProgressPercent() + "%";
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int textX = 20 + (barW - fm.stringWidth(percentText)) / 2;
        int textY = barY + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();

        g2.setColor(Color.BLACK);
        int thickness = 2;
        for (int x = -thickness; x <= thickness; x++) {
            for (int y = -thickness; y <= thickness; y++) {
                if (x != 0 || y != 0) {
                    g2.drawString(percentText, textX + x, textY + y);
                }
            }
        }

        g2.setColor(Color.WHITE);
        g2.drawString(percentText, textX, textY);
        g2.dispose();
    }

    private JPanel createValueDisplay(String tag, JLabel valLbl, int size, boolean isBold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel tagLbl = new JLabel(tag);
        tagLbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tagLbl.setForeground(GLOW_DARK);
        valLbl.setFont(new Font("Monospaced", isBold ? Font.BOLD : Font.PLAIN, size));
        valLbl.setForeground(DASH_GREEN);
        if (isBold) {
            valLbl.setBorder(BorderFactory.createLineBorder(GLOW_DARK, 1));
        }
        p.add(tagLbl, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        return p;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 18));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setPreferredSize(new Dimension(45, 30));
        b.setBorder(BorderFactory.createLineBorder(DASH_GREEN, 1));
        return b;
    }

    private int getProgressPercent() {
        return (int) Math.min(100, (goal.getProgress() / goal.getTarget()) * 100);
    }

    private String getEncouragementMessage() {
        double p = getProgressPercent();
        if (p >= 100) {
            return "Goal achieved!";
        } else if (p >= 75) {
            return "Almost there! Keep going!";
        } else if (p >= 50) {
            return "You're halfway there!";
        } else if (p > 0) {
            return "Good start, keep progressing!";
        } else {
            return "Start your journey!";
        }
    }

    private void handleDeposit() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        GoalsTab parentTab = (GoalsTab) SwingUtilities.getAncestorOfClass(GoalsTab.class, this);
        if (parentTab == null) {
            return;
        }

        double cashOnHand = parentTab.getDashboardTab().getCurrentTotalBalance();
        double currentProgress = goal.getProgress();
        double targetGoal = goal.getTarget();
        double remainingNeeded = targetGoal - currentProgress;

        DarkDepositDialog dialog = new DarkDepositDialog(parentWindow, goal.getName(), cashOnHand, currentProgress, targetGoal);
        dialog.setVisible(true);

        double amount = dialog.getAmount();
        if (amount != -1) {
            if (amount <= 0 || amount > cashOnHand || amount > remainingNeeded) {
                JOptionPane.showMessageDialog(parentWindow, "Invalid transaction amount.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = Integer.parseInt(AccountManager.getUserId());
            if (DataHandler.fundGoal(userId, goal.getGoalID(), amount, goal.getName())) {
                if (parentWindow instanceof BudgetTracker) {
                    ((BudgetTracker) parentWindow).refreshAllTabs();
                }
            }
        }
    }

    public void setRemoveAction(Runnable action) {
        this.removeAction = action;
    }

    class DarkDepositDialog extends JDialog {

        private double amount = -1;
        private JTextField inputField;
        private boolean confirmed = false;

        public DarkDepositDialog(Window parent, String goalName, double balance, double saved, double target) {
            super(parent, "DEPOSIT MOD", ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setLayout(new BorderLayout());
            setBackground(new Color(0, 0, 0, 0));

            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(5, 15, 5));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                    g2.setColor(new Color(59, 255, 59));
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                    g2.dispose();
                }
            };
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

            // HEADER
            JLabel title = new JLabel("DEPOSIT TO " + goalName.toUpperCase());
            title.setFont(new Font("Monospaced", Font.BOLD, 16));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(CENTER_ALIGNMENT);

            // DATA GRID (Balance, Saved, Needed)
            JPanel stats = new JPanel(new GridLayout(3, 1, 5, 5));
            stats.setOpaque(false);
            stats.add(createStatLabel("WALLET BALANCE: ", balance, new Color(59, 255, 59)));
            stats.add(createStatLabel("CURRENT SAVED:  ", saved, Color.CYAN));
            stats.add(createStatLabel("NEEDED LEFT:    ", (target - saved), Color.ORANGE));

            // INPUT FIELD
            inputField = new JTextField();
            inputField.setFont(new Font("Monospaced", Font.BOLD, 32));
            inputField.setBackground(Color.BLACK);
            inputField.setForeground(new Color(59, 255, 59));
            inputField.setCaretColor(Color.WHITE);
            inputField.setHorizontalAlignment(JTextField.CENTER);
            inputField.setBorder(BorderFactory.createLineBorder(new Color(59, 255, 59), 2));
            inputField.setMaximumSize(new Dimension(350, 60));

            // ACTION BUTTONS
            JButton confirmBtn = new JButton("CONFIRM");
            confirmBtn.setFont(new Font("Monospaced", Font.BOLD, 18));
            confirmBtn.setBackground(new Color(0, 80, 0));
            confirmBtn.setForeground(Color.WHITE);
            confirmBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });

            JButton cancelBtn = new JButton("CANCEL");
            cancelBtn.setFont(new Font("Monospaced", Font.PLAIN, 14));
            cancelBtn.setBackground(new Color(60, 0, 0));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.addActionListener(e -> dispose());

            JPanel bp = new JPanel(new FlowLayout());
            bp.setOpaque(false);
            bp.add(confirmBtn);
            bp.add(cancelBtn);

            content.add(title);
            content.add(Box.createVerticalStrut(20));
            content.add(stats);
            content.add(Box.createVerticalStrut(25));
            content.add(inputField);
            content.add(Box.createVerticalStrut(20));
            content.add(bp);

            add(content);
            pack();
            setLocationRelativeTo(parent);
        }

        private JLabel createStatLabel(String label, double value, Color valColor) {
            JLabel l = new JLabel(String.format("%s ₱%,.2f", label, value));
            l.setFont(new Font("Monospaced", Font.BOLD, 18));
            l.setForeground(valColor);
            l.setAlignmentX(LEFT_ALIGNMENT);
            return l;
        }

        public double getAmount() {
            if (!confirmed) {
                return -1;
            }
            try {
                return Double.parseDouble(inputField.getText().trim());
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
