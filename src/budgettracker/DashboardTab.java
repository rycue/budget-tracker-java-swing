package budgettracker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DashboardTab extends JPanel {

    private JLabel balanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JPanel historyPanel;
    private DefaultListModel<String> historyListModel;

    private double totalIncome = 0;
    private double totalExpense = 0;

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public DashboardTab() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);  // Main background
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.setBackground(new Color(17,17,17)); // Dark panel background

        balanceLabel = new JLabel("Balance: ₱0.00", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        balanceLabel.setForeground(Color.GREEN); // Bright green text
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalIncomeLabel = new JLabel("Total Income: ₱0.00", SwingConstants.CENTER);
        totalIncomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        totalIncomeLabel.setForeground(Color.GREEN);
        totalIncomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalExpenseLabel = new JLabel("Total Expense: ₱0.00", SwingConstants.CENTER);
        totalExpenseLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        totalExpenseLabel.setForeground(Color.GREEN);
        totalExpenseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(balanceLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(totalIncomeLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(totalExpenseLabel);

        add(topPanel, BorderLayout.NORTH);

        historyListModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyListModel);
        historyList.setBackground(Color.BLACK);
        historyList.setForeground(Color.GREEN);
        historyList.setSelectionBackground(new Color(0, 100, 0));
        historyList.setSelectionForeground(Color.WHITE);

        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN), "History", 0, 0, new Font("Arial", Font.BOLD, 16), Color.GREEN));
        historyPanel.setBackground(new Color(17,17,17));
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        add(historyPanel, BorderLayout.CENTER);
    }

    // Called when main window adds a transaction
    public void addTransactionFromOutside(Transaction t) {
        transactions.add(t);
        addToHistory(t);
        updateTotals();
    }

    private void addToHistory(Transaction t) {
        historyListModel.addElement(t.toString());
    }

    private void updateTotals() {
        totalIncome = transactions.stream()
                .filter(tx -> tx.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        totalExpense = transactions.stream()
                .filter(tx -> tx.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double balance = totalIncome - totalExpense;

        balanceLabel.setText(String.format("Balance: ₱%.2f", balance));
        totalIncomeLabel.setText(String.format("Total Income: ₱%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("Total Expense: ₱%.2f", totalExpense));
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }
}
