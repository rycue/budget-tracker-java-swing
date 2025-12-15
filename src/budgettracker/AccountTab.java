package budgettracker;

import javax.swing.*;
import java.awt.*;

public class AccountTab extends JPanel {

    private JLabel nameLbl, emailLbl, usernameLbl, passwordLbl, createdLbl;
    private JButton loginBtn;

    public AccountTab() {
        setLayout(new GridLayout(6, 1, 5, 5));
        setBackground(Color.decode("#121212"));

        nameLbl = new JLabel("Name: --");
        emailLbl = new JLabel("Email: --");
        usernameLbl = new JLabel("Username: --");
        passwordLbl = new JLabel("Password: --");
        createdLbl = new JLabel("Account Created: --");

        styleLabel(nameLbl);
        styleLabel(emailLbl);
        styleLabel(usernameLbl);
        styleLabel(passwordLbl);
        styleLabel(createdLbl);

        add(nameLbl);
        add(emailLbl);
        add(usernameLbl);
        add(passwordLbl);
        add(createdLbl);

        // Login + Manage Account button
        loginBtn = new JButton("Login / Manage Account");
        styleButton(loginBtn);

        loginBtn.addActionListener(e -> {
            LoginDialog login = new LoginDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            login.setSize(400, 300); // Make login bigger
            login.setVisible(true);
            refresh();
        });

        add(loginBtn);

        refresh();
    }

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.decode("#1b1b1b"));
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
    }

    public void refresh() {
        UserAccount user = AccountManager.getUser();
        if (user != null) {
            nameLbl.setText("Name: " + user.getFullName());
            emailLbl.setText("Email: " + user.getEmail());
            usernameLbl.setText("Username: " + user.getUsername());
            passwordLbl.setText("Password: " + user.getPassword());
            createdLbl.setText("Account Created: " + user.getDateCreated());
        } else {
            nameLbl.setText("Name: --");
            emailLbl.setText("Email: --");
            usernameLbl.setText("Username: --");
            passwordLbl.setText("Password: --");
            createdLbl.setText("Account Created: --");
        }
    }
}
