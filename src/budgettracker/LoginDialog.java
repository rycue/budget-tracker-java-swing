package budgettracker;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;

    private boolean success = false;

    public LoginDialog(JFrame parent) {
        super(parent, "Login", true);
        setSize(450, 350); // Optimized size
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#121212")); // Dark background

        //Main form panel
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

        // Username
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        userPanel.setBackground(Color.decode("#121212"));
        JLabel userLbl = new JLabel("Username:");
        userLbl.setForeground(Color.GREEN);
        userLbl.setFont(new Font("Arial", Font.BOLD, 16));
        usernameField = new JTextField();
        userPanel.add(userLbl, BorderLayout.NORTH);
        userPanel.add(usernameField, BorderLayout.CENTER);
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Password
        JPanel passPanel = new JPanel(new BorderLayout(5, 5));
        passPanel.setBackground(Color.decode("#121212"));
        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.GREEN);
        passLbl.setFont(new Font("Arial", Font.BOLD, 16));
        passwordField = new JPasswordField();
        passPanel.add(passLbl, BorderLayout.NORTH);
        passPanel.add(passwordField, BorderLayout.CENTER);
        mainPanel.add(passPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        add(mainPanel, BorderLayout.CENTER);

        //Buttons Panel with bottom margin
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15)); // Added vertical gap for bottom margin
        buttonPanel.setBackground(Color.decode("#121212"));

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton forgotBtn = new JButton("Forgot Password?");
        styleButton(loginBtn);
        styleButton(registerBtn);
        styleButton(forgotBtn);

        loginBtn.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (AccountManager.login(user, pass)) {
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        forgotBtn.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(forgotBtn);

        add(buttonPanel, BorderLayout.SOUTH);
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
