package budgettracker;

import javax.swing.*;
import java.awt.*;

public class GoalDialog extends JDialog {

    private JComboBox<String> categoryBox;
    private JTextField priceField;
    private JTextField dateField;
    private JTextField noteField;

    private boolean saved = false;

    private final String[] categories = {
            "Emergency Fund", "Vacation", "Savings", "Education",
            "Car", "House", "Shopping", "Other"
    };

    public GoalDialog(Window parent) {
        super(parent, "Add Goal", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        setSize(350, 300);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.decode("#121212"));

        //CATEGORY
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.setBackground(Color.decode("#121212"));
        categoryPanel.add(new JLabel("Category: ") {{ setForeground(Color.GREEN); }});
        categoryBox = new JComboBox<>(categories);
        categoryPanel.add(categoryBox);

        //PRICE
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.decode("#121212"));
        pricePanel.add(new JLabel("Price: ") {{ setForeground(Color.GREEN); }});
        priceField = new JTextField(10);
        pricePanel.add(priceField);

        //DATE
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(Color.decode("#121212"));
        datePanel.add(new JLabel("Date (YYYY-MM-DD): ") {{ setForeground(Color.GREEN); }});
        dateField = new JTextField(10);
        datePanel.add(dateField);

        //NOTE
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notePanel.setBackground(Color.decode("#121212"));
        notePanel.add(new JLabel("Note: ") {{ setForeground(Color.GREEN); }});
        noteField = new JTextField(20);
        notePanel.add(noteField);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.decode("#121212"));
        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save");
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());

        saveBtn.addActionListener(e -> {
            if (priceField.getText().isBlank() || dateField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Price and Date are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Price must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saved = true;
            dispose();
        });

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.decode("#121212"));
        formPanel.add(categoryPanel);
        formPanel.add(pricePanel);
        formPanel.add(datePanel);
        formPanel.add(notePanel);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
    public GoalDialog(Window parent, Goal goalToEdit) {
        this(parent);

        setTitle("Edit Goal");

        // Pre-fill fields
        categoryBox.setSelectedItem(goalToEdit.getCategory());
        priceField.setText(String.valueOf(goalToEdit.getPrice()));
        dateField.setText(goalToEdit.getDate());
        noteField.setText(goalToEdit.getNote());
    }

    public boolean isSaved() { return saved; }

    public Goal getGoal() {
        String category = (String) categoryBox.getSelectedItem();
        double price = Double.parseDouble(priceField.getText());
        String date = dateField.getText();
        String note = noteField.getText();
        return new Goal(category, price, date, note);
    }
}