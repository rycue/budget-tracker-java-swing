package budgettracker;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class LoginDialog extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private boolean success = false;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public LoginDialog(JFrame parent) {
        super(parent, "Login", true);
        setSize(550, 350); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false); 
        getContentPane().setBackground(Color.decode("#121212"));
        
        // Main form panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.decode("#121212"));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Title
        JLabel title = new JLabel("Budget Tracker Login", SwingConstants.CENTER);
        title.setForeground(Color.GREEN);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Email
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        userPanel.setBackground(Color.decode("#121212"));
        JLabel userLbl = new JLabel("Email:");
        userLbl.setForeground(Color.GREEN);
        userLbl.setFont(new Font("Arial", Font.BOLD, 16));
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 25)); 
        userPanel.add(userLbl, BorderLayout.NORTH);
        userPanel.add(emailField, BorderLayout.CENTER);
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        JPanel passPanel = new JPanel(new BorderLayout(5, 5));
        passPanel.setBackground(Color.decode("#121212"));
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.GREEN);
        passLbl.setFont(new Font("Arial", Font.BOLD, 16));
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 25)); 
        passPanel.add(passLbl, BorderLayout.NORTH);
        passPanel.add(passwordField, BorderLayout.CENTER);
        mainPanel.add(passPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        add(mainPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.decode("#121212"));
        
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton forgotBtn = new JButton("Forgot Password?");
        
        styleButton(loginBtn);
        styleButton(registerBtn);
        styleButton(forgotBtn);
        
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass = new String(passwordField.getPassword());
            
            if (email.isBlank()) {
                showError("Email is required.");
                return;
            }
            
            if (!isValidEmail(email)) {
                showError("Please enter a valid email address.");
                return;
            }
            
            if (pass.isBlank()) {
                showError("Password is required.");
                return;
            }
            
            if (AccountManager.loginWithEmail(email.toLowerCase(), pass)) {
                success = true;
                dispose();
            } else {
                showError("Invalid email or password!");
            }
        });
        
        registerBtn.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        
        forgotBtn.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(forgotBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void styleButton(JButton btn) {
        btn.setBackground(Color.decode("#1b1b1b"));
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 35));
    }
    
    public boolean isSuccess() {
        return success;
    }
}