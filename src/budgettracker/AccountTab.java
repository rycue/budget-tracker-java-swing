package budgettracker;

import javax.swing.*;
import java.awt.*;

public class AccountTab extends JPanel {

    private JLabel nameLbl, emailLbl, usernameLbl, passwordLbl, createdLbl;
    private JPasswordField passwordField;
    private JButton logoutBtn, toggleBtn;

    public AccountTab() {
        setBackground(Color.decode("#121212"));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize labels
        nameLbl = new JLabel("Name: --");
        emailLbl = new JLabel("Email: --");
        usernameLbl = new JLabel("Username: --");
        passwordLbl = new JLabel("Password:");
        createdLbl = new JLabel("Account Created: --");

        styleLabel(nameLbl);
        styleLabel(emailLbl);
        styleLabel(usernameLbl);
        styleLabel(passwordLbl);
        styleLabel(createdLbl);

        int row = 0;

        // Name
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(nameLbl, gbc);

        // Email
        gbc.gridy = row++;
        add(emailLbl, gbc);

        // Username
        gbc.gridy = row++;
        add(usernameLbl, gbc);

        // Password label and field side by side
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(passwordLbl, gbc);

        // Password field panel with eye icon
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.decode("#121212"));

        passwordField = new JPasswordField(20);
        passwordField.setText("--"); // default value
        passwordField.setForeground(Color.GREEN);
        passwordField.setBackground(Color.decode("#121212"));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setEchoChar('•'); // masked by default

        toggleBtn = new JButton("👁"); // Unicode eye
        toggleBtn.setPreferredSize(new Dimension(40, passwordField.getPreferredSize().height));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBackground(Color.decode("#1b1b1b"));
        toggleBtn.setForeground(Color.GREEN);

        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•'); // hide
            } else {
                passwordField.setEchoChar((char) 0); // show
            }
        });

        passPanel.add(passwordField, BorderLayout.CENTER);
        passPanel.add(toggleBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(passPanel, gbc);

        // Account created
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createdLbl, gbc);

        // Logout button
        logoutBtn = new JButton("Logout");
        styleButton(logoutBtn);
        gbc.gridy = row++;
        gbc.insets = new Insets(15, 5, 5, 5);
        add(logoutBtn, gbc);

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                AccountManager.logout();
                refresh();
                
                // Show login dialog after logout
                LoginDialog login = new LoginDialog((JFrame) SwingUtilities.getWindowAncestor(this));
                login.setSize(400, 300);
                login.setVisible(true);
                refresh();
            }
        });

        refresh();
    }

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("Arial", Font.PLAIN, 20));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.decode("#1b1b1b"));
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
    }

    public void refresh() {
        UserAccount user = AccountManager.getUser();
        if (user != null) {
            nameLbl.setText("Name: " + user.getFullName());
            emailLbl.setText("Email: " + user.getEmail());
            usernameLbl.setText("Username: " + user.getUsername());
            passwordField.setText(user.getPassword());
            createdLbl.setText("Account Created: " + user.getDateCreated());
            logoutBtn.setEnabled(true);
        } else {
            nameLbl.setText("Name: --");
            emailLbl.setText("Email: --");
            usernameLbl.setText("Username: --");
            passwordField.setText("--");
            createdLbl.setText("Account Created: --");
            logoutBtn.setEnabled(false);
        }
    }
}