package budgettracker;

import javax.swing.*;
import java.awt.*;

public class RegisterDialogOld extends JDialog {

    private JTextField nameField, mobileField, emailField, userField;
    private JPasswordField passField;

    //NEW FIELDS
    private JTextField questionField, answerField;

    public RegisterDialogOld(Window parent) {
        super(parent, "Register", ModalityType.APPLICATION_MODAL);
        setSize(350, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(makeField("Name: ", nameField = new JTextField(15)));
        form.add(makeField("Email: ", emailField = new JTextField(15)));
        form.add(makeField("Username: ", userField = new JTextField(15)));
        form.add(makeField("Password: ", passField = new JPasswordField(15)));

        //SECURITY QUESTION + ANSWER
        form.add(makeField("Security Question: ", questionField = new JTextField(15)));
        form.add(makeField("Security Answer: ", answerField = new JTextField(15)));

        JButton registerBtn = new JButton("Create Account");

        registerBtn.addActionListener(e -> {

            // Basic validation
            if (nameField.getText().isBlank() ||
                mobileField.getText().isBlank() ||
                emailField.getText().isBlank() ||
                userField.getText().isBlank() ||
                new String(passField.getPassword()).isBlank() ||
                questionField.getText().isBlank() ||
                answerField.getText().isBlank()) 
            {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            // Create the user with security question & answer
            UserAccount user = new UserAccount(
                    nameField.getText(),
                    mobileField.getText(),
                    emailField.getText(),
                    userField.getText(),
                    new String(passField.getPassword()),
                    questionField.getText(),    
                    answerField.getText()       
            );

            // Register user
            AccountManager.register(user);

            JOptionPane.showMessageDialog(this, "Account created!");
            dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(registerBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel makeField(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel(label));
        p.add(field);
        return p;
    }
}
