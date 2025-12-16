package budgettracker;

import javax.swing.*;
import java.awt.*;

public class AccountTab extends JPanel {

    private JLabel nameLbl, emailLbl, passwordLbl, createdLbl;
    private JPasswordField passwordField;
    private JButton logoutBtn, toggleBtn, editProfileBtn;

    public AccountTab() {
        setBackground(Color.decode("#121212"));
        setLayout(new BorderLayout());

        // Main container with centered content
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.decode("#121212"));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // PROFILE CARD
        JPanel profileCard = createCard("Profile Information");
        nameLbl = new JLabel("Name: --");
        emailLbl = new JLabel("Email: --");
        styleLabel(nameLbl);
        styleLabel(emailLbl);
        
        profileCard.add(nameLbl);
        profileCard.add(Box.createVerticalStrut(10));
        profileCard.add(emailLbl);

        // SECURITY CARD
        JPanel securityCard = createCard("Security");
        
        // Password label and field container
        JPanel passwordContainer = new JPanel();
        passwordContainer.setLayout(new BoxLayout(passwordContainer, BoxLayout.Y_AXIS));
        passwordContainer.setBackground(Color.decode("#1b1b1b"));
        passwordContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordLbl = new JLabel("Password:");
        styleLabel(passwordLbl);
        passwordContainer.add(passwordLbl);
        passwordContainer.add(Box.createVerticalStrut(10));
        
        JPanel passPanel = new JPanel(new BorderLayout(10, 0));
        passPanel.setBackground(Color.decode("#2a2a2a"));
        passPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        passwordField = new JPasswordField(20);
        passwordField.setText("--");
        passwordField.setForeground(Color.GREEN);
        passwordField.setBackground(Color.decode("#2a2a2a"));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setEchoChar('•');

        toggleBtn = new JButton("👁");
        toggleBtn.setPreferredSize(new Dimension(40, 30));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBackground(Color.decode("#3a3a3a"));
        toggleBtn.setForeground(Color.GREEN);
        toggleBtn.setBorder(BorderFactory.createLineBorder(Color.decode("#4a4a4a"), 1));

        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == (char) 0) {
                passwordField.setEchoChar('•');
            } else {
                passwordField.setEchoChar((char) 0);
            }
        });

        passPanel.add(passwordField, BorderLayout.CENTER);
        passPanel.add(toggleBtn, BorderLayout.EAST);
        
        passwordContainer.add(passPanel);
        securityCard.add(passwordContainer);

        JPanel accountCard = createCard("Account Details");
        createdLbl = new JLabel("Account Created: --");
        styleLabel(createdLbl);
        
        accountCard.add(createdLbl);

        // ACTIONS CARD
        JPanel actionsCard = createCard("Actions");
        
        // Button container for buttons on the same line
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonContainer.setBackground(Color.decode("#1b1b1b"));
        buttonContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        editProfileBtn = new JButton("Edit Profile");
        styleActionButton(editProfileBtn);
        
        editProfileBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Edit Profile feature coming soon!",
                "Edit Profile",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        logoutBtn = new JButton("Logout");
        styleActionButton(logoutBtn);
        
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
                
                JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                mainFrame.dispose();
                
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                
                if (login.isSuccess()) {
                    new BudgetTrackerGUI().setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });

        buttonContainer.add(editProfileBtn);
        buttonContainer.add(logoutBtn);
        
        actionsCard.add(buttonContainer);

        mainContainer.add(profileCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(securityCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(accountCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(actionsCard);
        mainContainer.add(Box.createVerticalGlue()); // Push everything to top

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#1b1b1b"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#2a2a2a"), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Card title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.decode("#2a2a2a"));
        separator.setBackground(Color.decode("#2a2a2a"));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(separator);
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleActionButton(JButton btn) {
        btn.setBackground(Color.decode("#2a2a2a"));
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GREEN, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.GREEN);
                btn.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.decode("#2a2a2a"));
                btn.setForeground(Color.GREEN);
            }
        });
    }

    public void refresh() {
        UserAccount user = AccountManager.getUser();
        if (user != null) {
            nameLbl.setText("Name: " + user.getFullName());
            emailLbl.setText("Email: " + user.getEmail());
            passwordField.setText(user.getPassword());
            createdLbl.setText("Account Created: " + user.getDateCreated());
            logoutBtn.setEnabled(true);
            editProfileBtn.setEnabled(true);
        } else {
            nameLbl.setText("Name: --");
            emailLbl.setText("Email: --");
            passwordField.setText("--");
            createdLbl.setText("Account Created: --");
            logoutBtn.setEnabled(false);
            editProfileBtn.setEnabled(false);
        }
    }
}