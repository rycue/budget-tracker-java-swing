package budgettracker;

import javax.swing.*;
import java.awt.*;

import java.util.regex.Pattern;

public class LoginDialog extends JDialog {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton togglePasswordBtn;
    private boolean success = false;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public void setExternalSuccess(boolean success) {
        this.success = success;
    }

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

        JPanel emailContainer = new JPanel(new BorderLayout(5, 0));
        emailContainer.setBackground(Color.decode("#121212"));

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 25));

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(40, 25));
        spacer.setBackground(Color.decode("#121212"));

        emailContainer.add(emailField, BorderLayout.CENTER);
        emailContainer.add(spacer, BorderLayout.EAST);

        userPanel.add(userLbl, BorderLayout.NORTH);
        userPanel.add(emailContainer, BorderLayout.CENTER);
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Password with toggle button
        JPanel passPanel = new JPanel(new BorderLayout(5, 5));
        passPanel.setBackground(Color.decode("#121212"));
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.GREEN);
        passLbl.setFont(new Font("Arial", Font.BOLD, 16));

        // Password field container with toggle button
        JPanel passwordContainer = new JPanel(new BorderLayout(5, 0));
        passwordContainer.setBackground(Color.decode("#121212"));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 25));

        togglePasswordBtn = new JButton("👁");
        togglePasswordBtn.setPreferredSize(new Dimension(40, 25));
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setBackground(Color.decode("#2a2a2a"));
        togglePasswordBtn.setForeground(Color.GREEN);
        togglePasswordBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#4a4a4a"), 1));
        togglePasswordBtn.setToolTipText("Show/Hide Password");

        togglePasswordBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
                togglePasswordBtn.setText("👁");
            } else {
                passwordField.setEchoChar((char) 0);
                togglePasswordBtn.setText("🙈");
            }
        });

        passwordContainer.add(passwordField, BorderLayout.CENTER);
        passwordContainer.add(togglePasswordBtn, BorderLayout.EAST);

        passPanel.add(passLbl, BorderLayout.NORTH);
        passPanel.add(passwordContainer, BorderLayout.CENTER);
        mainPanel.add(passPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.decode("#121212"));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridy = 0;
        gbc.weighty = 1.0;

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

        forgotBtn.addActionListener(e -> {
            String email = emailField.getText().trim();

            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your email first so we can find your account.");
                return;
            }

            // Best Practice: Don't trust local state; check the DB
            UserAccount targetUser = SQLConnector.getInstance().findUserByEmail(email);

            if (targetUser != null) {
                // Pass the found user into the Dialog constructor
                ForgotPasswordDialog dialog = new ForgotPasswordDialog(this, targetUser);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "No account found with that email.");
            }
        });
        

        gbc.gridx = 0;
        gbc.weightx = 0.25;
        buttonPanel.add(loginBtn, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.30;
        buttonPanel.add(registerBtn, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.45;
        buttonPanel.add(forgotBtn, gbc);

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
        btn.setPreferredSize(new Dimension(0, 35));
    }

    public boolean isSuccess() {
        return success;
    }
}
