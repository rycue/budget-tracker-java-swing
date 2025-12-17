package budgettracker;

import javax.swing.*;
import java.awt.*;

public class AccountTab extends JPanel {

    private JLabel nameLbl, emailLbl, passwordLbl, createdLbl;
    private JPasswordField passwordField;
    private JButton logoutBtn, toggleBtn, editProfileBtn, resetDataBtn, changePasswordBtn, deleteAccountBtn;

    public AccountTab() {
        // 1. INITIALIZE BUTTONS
        editProfileBtn = new JButton("Edit Profile");
        logoutBtn = new JButton("Logout");
        changePasswordBtn = new JButton("Change Password");
        resetDataBtn = new JButton("Reset Data");
        deleteAccountBtn = new JButton("Delete Account");

        styleActionButton(editProfileBtn);
        styleActionButton(logoutBtn);
        styleActionButton(changePasswordBtn);
        styleActionButton(resetDataBtn);
        styleActionButton(deleteAccountBtn);

        // 2. MAIN PANEL SETUP
        setBackground(Color.decode("#121212"));
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(Color.decode("#121212"));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 3. PROFILE CARD
        JPanel profileCard = createCard("Profile Information");
        nameLbl = new JLabel("Name: --");
        emailLbl = new JLabel("Email: --");
        styleLabel(nameLbl);
        styleLabel(emailLbl);
        profileCard.add(nameLbl);
        profileCard.add(Box.createVerticalStrut(10));
        profileCard.add(emailLbl);

        // 4. SECURITY CARD
        JPanel securityCard = createCard("Security");
        passwordLbl = new JLabel("Password:");
        styleLabel(passwordLbl);

        JPanel passPanel = new JPanel(new BorderLayout(10, 0));
        passPanel.setBackground(Color.decode("#2a2a2a"));
        passPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        

        passwordField = new JPasswordField(20);
        passwordField.setText("------------");
        passwordField.setEditable(false);
        passwordField.setForeground(Color.GREEN);
        passwordField.setBackground(Color.decode("#2a2a2a"));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setEchoChar('•');
        
        passwordField.setMinimumSize(new Dimension(200, 30));
        passwordField.setPreferredSize(new Dimension(300, 30));

        toggleBtn = new JButton("👁");
        toggleBtn.setPreferredSize(new Dimension(60, 30));
        toggleBtn.setBackground(Color.decode("#3a3a3a"));
        toggleBtn.setForeground(Color.GREEN);
        toggleBtn.addActionListener(e -> {
            UserAccount user = AccountManager.getUser();
            if (passwordField.getEchoChar() == '•') {
                passwordField.setEchoChar((char) 0);
                passwordField.setText(user.getPassword());
                toggleBtn.setText("🔒"); // Change icon to locked
            } else {
                // HIDE: Go back to bullets
                passwordField.setEchoChar('•');
                passwordField.setText("**********");
                toggleBtn.setText("👁");
            }
        });

        passPanel.add(passwordField, BorderLayout.CENTER);
        passPanel.add(toggleBtn, BorderLayout.EAST);

        securityCard.add(passwordLbl);
        securityCard.add(Box.createVerticalStrut(10));
        securityCard.add(passPanel);

        // 5. ACCOUNT DETAILS CARD
        JPanel accountCard = createCard("Account Details");
        createdLbl = new JLabel("Account Created: --");
        styleLabel(createdLbl);
        accountCard.add(createdLbl);

        // 6. ACTIONS CARD
        JPanel actionsCard = createCard("Actions");
        JPanel actionButtonBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        actionButtonBox.setBackground(Color.decode("#1b1b1b"));
        actionButtonBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionButtonBox.add(editProfileBtn);
        actionButtonBox.add(Box.createHorizontalStrut(10));
        actionButtonBox.add(logoutBtn);
        actionsCard.add(actionButtonBox);

        // 7. DANGER ZONE CARD
        JPanel dangerCard = createCard("Danger Zone");
        styleDangerTitle(dangerCard);

        JPanel dangerButtonBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        dangerButtonBox.setBackground(Color.decode("#1b1b1b"));
        dangerButtonBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        dangerButtonBox.add(resetDataBtn);
        dangerButtonBox.add(Box.createHorizontalStrut(10));
        dangerButtonBox.add(deleteAccountBtn);
        dangerCard.add(dangerButtonBox);

        // 8. ADD EVERYTHING TO MAIN VIEW
        mainContainer.add(profileCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(securityCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(accountCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(actionsCard);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(dangerCard);
        mainContainer.add(Box.createVerticalGlue());

        // 9. LOGIC - RESTORED LOGOUT & EDIT POPUP
        editProfileBtn.addActionListener(e -> showEditProfileDialog());

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AccountManager.setUserId(null);

                Window mainFrame = SwingUtilities.getWindowAncestor(this);
                mainFrame.dispose();

                BudgetTracker.showLogin();
            }
        });

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    private void showEditProfileDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.getContentPane().setBackground(Color.decode("#1b1b1b"));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.decode("#1b1b1b"));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel("Full Name:");
        styleLabel(nameLabel);
        JTextField nameField = new JTextField(20);
        nameField.setBackground(Color.decode("#2a2a2a"));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(nameLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(nameField);
        content.add(Box.createVerticalStrut(20));

        JLabel secLabel = new JLabel("Security:");
        styleLabel(secLabel);
        content.add(secLabel);
        content.add(Box.createVerticalStrut(10));

        changePasswordBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(changePasswordBtn);

        content.add(Box.createVerticalStrut(30));
        JButton saveBtn = new JButton("Save Changes");
        styleActionButton(saveBtn);
        saveBtn.addActionListener(e -> dialog.dispose());
        content.add(saveBtn);

        dialog.add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));

        JSeparator sep = new JSeparator();
        sep.setForeground(Color.decode("#2a2a2a"));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
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
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setMaximumSize(new Dimension(180, 45));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        Dimension size = new Dimension(200, 45);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

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

    private void styleDangerTitle(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(Color.RED);
            }
        }
    }

    public void refresh() {
        UserAccount user = AccountManager.getUser();
        if (user != null) {
            String pass = user.getPassword();
            System.out.println("Debug: Password retrieved is: [" + user.getPassword() + "]");

            if (pass != null && !pass.isEmpty()) {
                passwordField.setText(pass);
            } else {
                passwordField.setText("********");
            }

            nameLbl.setText("Name: " + user.getFullName());
            emailLbl.setText("Email: " + user.getEmail());
            createdLbl.setText("Account Created: " + user.getDateCreated());
        }
    }
}
