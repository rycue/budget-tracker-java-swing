package budgettracker;

import javax.swing.*;
import java.awt.*;

public class TransactionDialog extends JDialog {

    private JRadioButton incomeBtn, expenseBtn;
    private JComboBox<String> categoryBox;
    private JTextField noteField;
    private JTextField amountField;

    private boolean saved = false;

    private final String[] incomeCategories = {"Salary", "Bonus", "Investment", "Gift", "Other"};
    private final String[] expenseCategories = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Other"};

    public TransactionDialog(JFrame parent) {
        super(parent, "Record Transaction", true);
        setLayout(new BorderLayout(10, 10));
        setSize(350, 320);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.decode("#121212"));

        // TYPE
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(Color.decode("#121212"));
        incomeBtn = new JRadioButton("Income");
        expenseBtn = new JRadioButton("Expense");
        incomeBtn.setForeground(Color.BLACK);
        expenseBtn.setForeground(Color.BLACK);
        ButtonGroup group = new ButtonGroup();
        group.add(incomeBtn);
        group.add(expenseBtn);
        incomeBtn.setSelected(true);
        typePanel.add(new JLabel("Type: ") {{ setForeground(Color.GREEN); }});
        typePanel.add(incomeBtn);
        typePanel.add(expenseBtn);

        //CATEGORY
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.setBackground(Color.decode("#121212"));
        categoryPanel.add(new JLabel("Category: ") {{ setForeground(Color.GREEN); }});
        categoryBox = new JComboBox<>(incomeCategories);
        categoryPanel.add(categoryBox);

        incomeBtn.addActionListener(e -> categoryBox.setModel(new DefaultComboBoxModel<>(incomeCategories)));
        expenseBtn.addActionListener(e -> categoryBox.setModel(new DefaultComboBoxModel<>(expenseCategories)));

        //NOTE
        JPanel notePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notePanel.setBackground(Color.decode("#121212"));
        notePanel.add(new JLabel("Note: ") {{ setForeground(Color.GREEN); }});
        noteField = new JTextField(20);
        notePanel.add(noteField);

        //AMOUNT
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        amountPanel.setBackground(Color.decode("#121212"));
        amountPanel.add(new JLabel("Amount: ") {{ setForeground(Color.GREEN); }});
        amountField = new JTextField(10);
        amountPanel.add(amountField);

        //BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.decode("#121212"));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());

        saveBtn.addActionListener(e -> {
            if (amountField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Amount is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try { Double.parseDouble(amountField.getText()); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            saved = true;
            dispose();
        });

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.decode("#121212"));
        formPanel.add(typePanel);
        formPanel.add(categoryPanel);
        formPanel.add(notePanel);
        formPanel.add(amountPanel);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isSaved() { return saved; }

    public Transaction getTransaction() {
        String selected = incomeBtn.isSelected() ? "Income" : "Expense";
        Transaction.Type type = selected.equals("Income") ? Transaction.Type.INCOME : Transaction.Type.EXPENSE;
        String category = (String) categoryBox.getSelectedItem();
        String note = noteField.getText();
        double amount = Double.parseDouble(amountField.getText());
        return new Transaction(type, category, note, amount);
    }
}
