package budgettracker;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {

    private final UserAccount targetUser;
    private final JPanel mainPanel;
    private final JTextField answerField = new JTextField(15);
    private final JPasswordField newPassField = new JPasswordField(15);
    private final JButton actionBtn = new JButton("Verify Identity");
    private boolean isStepOne = true;

    public ForgotPasswordDialog(Window parent, UserAccount user) {
        super(parent, "Account Recovery", ModalityType.APPLICATION_MODAL);
        this.targetUser = user;

        getContentPane().setBackground(Color.decode("#121212"));
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#121212"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        showStep1();

        add(mainPanel);
        pack(); // CRITICAL: Makes the window fit the components perfectly
        setLocationRelativeTo(parent);
    }

    private void showStep1() {
        mainPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel head = new JLabel("Security Question for " + targetUser.getEmail());
        head.setForeground(Color.GRAY);
        head.setFont(new Font("Arial", Font.PLAIN, 10));
        gbc.gridy = 0;
        mainPanel.add(head, gbc);

        JLabel question = new JLabel(targetUser.getSecurityQuestion());
        question.setForeground(Color.GREEN);
        question.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 15, 0);
        mainPanel.add(question, gbc);

        styleField(answerField);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(answerField, gbc);

        actionBtn.addActionListener(e -> handleFlow());
        gbc.gridy = 3;
        mainPanel.add(actionBtn, gbc);
    }

    private void handleFlow() {
        if (isStepOne) {
            if (targetUser.getSecurityAnswer().equalsIgnoreCase(answerField.getText().trim())) {
                isStepOne = false;
                showStep2();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect answer!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            String newPass = new String(newPassField.getPassword());
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password too short!");
                return;
            }
            if (AccountManager.resetPasswordExternal(targetUser, newPass)) {
                JOptionPane.showMessageDialog(this, "Success! Please login with your new password.");
                dispose();
            }
        }
    }

    private void showStep2() {
        mainPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("Enter New Password:");
        lbl.setForeground(Color.WHITE);
        gbc.gridy = 0;
        mainPanel.add(lbl, gbc);

        styleField(newPassField);
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 15, 0);
        mainPanel.add(newPassField, gbc);

        actionBtn.setText("Update Password");
        gbc.gridy = 2;
        mainPanel.add(actionBtn, gbc);

        mainPanel.revalidate();
        mainPanel.repaint();
        pack();
    }

    private void styleField(JTextField field) {
        field.setBackground(Color.decode("#2a2a2a"));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }
}
