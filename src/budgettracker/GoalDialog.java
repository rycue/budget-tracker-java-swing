package budgettracker;

import javax.swing.*;
import java.awt.*;

public class GoalDialog extends JDialog {

    private JTextField nameField;
    private JTextField targetField;
    private boolean saved = false;

    public GoalDialog(Window parent) {
        super(parent, "Add Goal", ModalityType.APPLICATION_MODAL);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(makeField("Goal Name: ", nameField = new JTextField(15)));
        form.add(makeField("Target Amount: ", targetField = new JTextField(15)));

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            if (nameField.getText().isBlank() || targetField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            try {
                Double.parseDouble(targetField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Target must be a number.");
                return;
            }
            saved = true;
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel makeField(String label, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(field);
        return panel;
    }

    public boolean isSaved() { return saved; }

    public Goal getGoal() {
        return new Goal(nameField.getText(), Double.parseDouble(targetField.getText()));
    }
}
