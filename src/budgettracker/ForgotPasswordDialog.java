package budgettracker;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {

    public ForgotPasswordDialog(Window parent) {
        super(parent, "Password Recovery", ModalityType.APPLICATION_MODAL);
        setSize(350, 180);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.decode("#121212"));

        UserAccount user = AccountManager.getUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "No user registered!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        JLabel questionLbl = new JLabel("Security Question: " + user.getSecurityQuestion());
        questionLbl.setForeground(Color.GREEN);
        questionLbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField answerField = new JTextField(20);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            if (user.getSecurityAnswer().equalsIgnoreCase(answerField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Your password is: " + user.getPassword());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect answer!");
            }
        });

        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.decode("#121212"));
        panel.add(answerField);

        add(questionLbl, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(submitBtn, BorderLayout.SOUTH);
    }
}
