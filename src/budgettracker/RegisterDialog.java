package budgettracker;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.regex.Pattern;

public class RegisterDialog extends JDialog {
    private final String[] securityQuestion = {"-- Select a question --", "What is the name of your first pet?", "What is your favorite food?", "What is your favorite place? "};
    private JTextField fullField, emailField;
    private JPasswordField passField;
    
    //NEW FIELDS
    private JComboBox<String> questionBox;
    private JTextField answerField;
    private JPanel answerPanel;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public RegisterDialog(Window parent) {
        super(parent, "Register", ModalityType.APPLICATION_MODAL);
        setSize(350, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.decode("#121212"));
        
        JPanel form = new JPanel();
        form.setBorder(BorderFactory.createEmptyBorder(30,2,2,2));
        form.setBackground(Color.decode("#121212"));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
                
        form.add(makeField("Full Name: ", fullField = new JTextField(15)));
        form.add(makeField("Email: ", emailField = new JTextField(15)));
        form.add(makeField("Password: ", passField = new JPasswordField(15)));
        
        questionBox = new JComboBox<>(securityQuestion);
        styleComboBox(questionBox);
        
        answerPanel = makeField("Security Answer: ", answerField = new JTextField(15));
        answerPanel.setVisible(false);
        
        questionBox.addActionListener(e -> {
            if (questionBox.getSelectedIndex() > 0) {
                answerPanel.setVisible(true);
            } else {
                answerPanel.setVisible(false);
            }
            form.revalidate();
            form.repaint();
        });
        
        form.add(makeField("Security Question: ", questionBox));
        form.add(answerPanel);
        
        JButton registerBtn = new JButton("Create Account");
        styleButton(registerBtn);
        
        registerBtn.addActionListener(e -> {
            
            if (fullField.getText().isBlank()) {
                showError("Full name is required.");
                return;
            }
            
            if (emailField.getText().isBlank()) {
                showError("Email is required.");
                return;
            }
            
            // Email format validation
            if (!isValidEmail(emailField.getText())) {
                showError("Please enter a valid email address.\nExample: user@example.com");
                return;
            }
            
            String password = new String(passField.getPassword());
            if (password.isBlank()) {
                showError("Password is required.");
                return;
            }
            
            // Password strength validation
            if (password.length() < 6) {
                showError("Password must be at least 6 characters long.");
                return;
            }
            
            if (questionBox.getSelectedIndex() == 0) {
                showError("Please select a security question.");
                return;
            }
            
            if (answerField.getText().isBlank()) {
                showError("Security answer is required.");
                return;
            }
            
            // Create the user with security question & answer
            UserAccount user = new UserAccount(
                fullField.getText().trim(),
                emailField.getText().trim().toLowerCase(),
                password,
                questionBox.getSelectedItem().toString(),
                answerField.getText().trim()
            );
            
            // Register user
            AccountManager.register(user);
            
            JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
        
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Color.decode("#121212"));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnPanel.add(registerBtn, BorderLayout.CENTER);
        
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private JPanel makeField(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(Color.decode("#121212"));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Style text fields
        if (field instanceof JTextComponent) {
            JTextComponent textField = (JTextComponent) field;
            textField.setBackground(Color.WHITE);
            textField.setForeground(Color.BLACK);
            textField.setCaretColor(Color.BLACK);
            textField.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        
        p.add(lbl);
        p.add(field);
        return p;
    }
    
    private void styleButton(JButton btn) {
        btn.setBackground(Color.decode("#1b1b1b"));
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(0, 45));
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.BLACK);
        combo.setFont(new Font("Arial", Font.PLAIN, 14));
    }
}