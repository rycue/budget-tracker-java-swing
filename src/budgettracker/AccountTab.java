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
        
        // 10. DELETE ACCOUNT
        deleteAccountBtn.addActionListener(e -> {
            // 1. Double Confirmation (Danger Zone!)
            int confirm = JOptionPane.showConfirmDialog(this, """
                                                              EXTREME DANGER: This will permanently delete your account and all financial history.
                                                              This action cannot be undone. Are you absolutely sure?""",
                    "Delete Account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String userIdStr = AccountManager.getUserId();
                if (userIdStr != null) {
                    int userId = Integer.parseInt(userIdStr);

                    // 2. Execute the Transactional Delete
                    boolean success = DataHandler.deleteFullAccount(userId);

                    if (success) {
                        JOptionPane.showMessageDialog(this, "Your account has been wiped from the system.");

                        // 3. Force Logout and redirect to Login
                        AccountManager.logout();
                        Window mainFrame = SwingUtilities.getWindowAncestor(this);
                        if (mainFrame != null) {
                            mainFrame.dispose();
                        }
                        BudgetTracker.showLogin();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: Could not delete account. Check database connection.");
                    }
                }
            }
        });
        
        // 11. CLEAR DATA
        resetDataBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, """
                                                              Are you sure you want to wipe all transactions and goals?
                                                              Your account profile will remain, but all data will be lost.""",
                    "Reset Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String userIdStr = AccountManager.getUserId();
                if (userIdStr != null) {
                    int userId = Integer.parseInt(userIdStr);

                    if (DataHandler.resetUserData(userId)) {
                        JOptionPane.showMessageDialog(this, "All data has been cleared.");
                        // Find the main BudgetTracker frame and tell it to refresh
                        Window parentWindow = SwingUtilities.getWindowAncestor(this);
                        if (parentWindow instanceof BudgetTracker) {
                            ((BudgetTracker) parentWindow).refreshAllTabs();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Reset failed. Check connection.");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    private void showEditProfileDialog() {
        UserAccount user = AccountManager.getUser();
        String originalName = user.getFullName(); // Store the current name to compare

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.getContentPane().setBackground(Color.decode("#1b1b1b"));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.decode("#1b1b1b"));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Full Name Field - PRE-FILLED
        JLabel nameLabel = new JLabel("Full Name:");
        styleLabel(nameLabel);
        JTextField nameField = new JTextField(originalName, 20); // Pre-fill with originalName
        styleTextField(nameField);

        // 2. Save Button - DISABLED BY DEFAULT
        JButton saveBtn = new JButton("Save Changes");
        styleActionButton(saveBtn);
        saveBtn.setEnabled(false); // Start disabled

        // 3. DocumentListener to detect changes
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void check() {
                // Only enable if name is different from original AND not empty
                String currentText = nameField.getText().trim();
                saveBtn.setEnabled(!currentText.equals(originalName) && !currentText.isEmpty());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                check();
            }
        });
        
        // Inside showEditProfileDialog() ...
// Step A: Clear any old listeners to prevent the "Double Popup"
        for (java.awt.event.ActionListener al : changePasswordBtn.getActionListeners()) {
            changePasswordBtn.removeActionListener(al);
        }

// Step B: Add the fresh listener for this specific dialog session
        changePasswordBtn.addActionListener(e -> {
            boolean success = performPasswordChange();
            if (success) {
                dialog.dispose(); // This closes the Edit Profile window
            }
        });


        // 5. Save Button Logic
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText().trim();
            int userId = Integer.parseInt(AccountManager.getUserId());

            if (DataHandler.updateUserName(userId, newName)) {
                AccountManager.updateLocalUser(newName);
                refresh();
                JOptionPane.showMessageDialog(dialog, "Profile Updated!");
                dialog.dispose();
            }
        });

        // UI Assembly
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(nameField);
        content.add(Box.createVerticalStrut(20));
        content.add(new JLabel("Security:"));
        content.add(Box.createVerticalStrut(10));
        content.add(changePasswordBtn);
        content.add(Box.createVerticalStrut(30));
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
    
    
    private boolean performPasswordChange() {
        // Create the masked fields
        JPasswordField pf1 = new JPasswordField();
        JPasswordField pf2 = new JPasswordField();

        // Style them to match your dark theme (optional but recommended)
        styleTextField(pf1);
        styleTextField(pf2);

        // Create a panel to hold the labels and fields
        Object[] message = {
            "New Password:", pf1,
            "Confirm New Password:", pf2
        };

        // Show the dialog with the password fields
        int option = JOptionPane.showConfirmDialog(this, message, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        String newPass = new String(pf1.getPassword());
        String confirmPass = new String(pf2.getPassword());

        if (newPass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty!");
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 3. Save
        int userId = Integer.parseInt(AccountManager.getUserId());
        if (DataHandler.updateUserPassword(userId, newPass)) {
            AccountManager.updateLocalPassword(newPass);
            refresh();
            JOptionPane.showMessageDialog(this, "Password changed successfully!");
            return true; // Success! This triggers the dialog.dispose()
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update password.");
            return false;
        }
    }
    
    
    private void styleTextField(JTextField field) {
        field.setBackground(Color.decode("#2a2a2a"));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
