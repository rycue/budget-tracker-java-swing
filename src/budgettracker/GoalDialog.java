package budgettracker;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;

public class GoalDialog extends JDialog {

    private JTextField nameField;
    private JTextField targetField;
    private boolean saved = false;
    private static final int MAX_NAME_LENGTH = 30;

    public GoalDialog(Window parent) {
        super(parent, "Add Goal", ModalityType.APPLICATION_MODAL);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        nameField = new JTextField(15);

        // Add character limit to name field
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                if ((fb.getDocument().getLength() + string.length()) <= MAX_NAME_LENGTH) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }
                int currentLength = fb.getDocument().getLength();
                int newLength = currentLength - length + text.length();
                if (newLength <= MAX_NAME_LENGTH) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        targetField = new JTextField(15);

        form.add(makeField("Goal Name: ", nameField));
        form.add(makeField("Target Amount: ", targetField));

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            if (nameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Goal name is required.");
                return;
            }

            if (targetField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Target amount is required.");
                return;
            }

            try {
                double target = Double.parseDouble(targetField.getText().trim());
                if (target <= 0) {
                    JOptionPane.showMessageDialog(this, "Target amount must be greater than zero.");
                    return;
                }
                if (target > 1000000000) {
                    JOptionPane.showMessageDialog(this, "Target amount is too large.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Target must be a valid number.");
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

    public boolean isSaved() {
        return saved;
    }

    public Goal getGoal() {
        return new Goal(nameField.getText().trim(), Double.parseDouble(targetField.getText().trim()));
    }
}
