package budgettracker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardTab extends JPanel {

    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JTable transactionTable;
    private DefaultTableModel tableModel;   
    private List<Transaction> transactions;
    private AnalyticsTab analyticsTab;
    private GoalsTab goalsTab;

    public DashboardTab() {  
        transactions = new ArrayList<>();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        // TOP PANEL
        // --- TOP PANEL REFACTOR ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.BLACK);
// CRITICAL: Add padding so text doesn't touch the window edges
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

// 1. Primary Stat: Monthly Savings
// We use a larger font and center it to create a clear focal point
        balanceLabel = createLabel("MONTHLY SAVINGS: ₱0.00", 28);
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0); // Bottom margin to separate from sub-stats
        topPanel.add(balanceLabel, gbc);

// 2. Sub-stats Container (Income & Expense)
// Using a 1x2 Grid with horizontal gaps for breathing room
        JPanel iePanel = new JPanel(new GridLayout(1, 2, 30, 0));
        iePanel.setBackground(Color.BLACK);

// Use specific color coding: Green for Income, Red-Orange for Expense
        incomeLabel = createLabel("INCOME: ₱0.00", 18);
        incomeLabel.setForeground(new Color(50, 205, 50)); // Lime Green
        incomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        expenseLabel = createLabel("EXPENSE: ₱0.00", 18);
        expenseLabel.setForeground(new Color(255, 69, 0)); // Red-Orange
        expenseLabel.setHorizontalAlignment(SwingConstants.CENTER);

        iePanel.add(incomeLabel);
        iePanel.add(expenseLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Reset margins
        topPanel.add(iePanel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // HISTORY PANEL WITH BORDER
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.BLACK);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2),
                "History",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.GREEN
        ));

        // TABLE
        String[] columns = {"Date", "Type", "Category", "Note", "Amount", "Remove"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the button column editable
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(30);

        // Button renderer/editor
        transactionTable.getColumn("Remove").setCellRenderer(new ButtonRenderer());
        transactionTable.getColumn("Remove").setCellEditor(new ButtonEditor());

        historyPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        add(historyPanel, BorderLayout.CENTER);
        // Dark Mode Table Styling
        transactionTable.setBackground(Color.BLACK);
        transactionTable.setForeground(Color.GREEN);
        transactionTable.setGridColor(Color.DARK_GRAY);
        transactionTable.getTableHeader().setBackground(Color.DARK_GRAY);
        transactionTable.getTableHeader().setForeground(Color.WHITE);

// Ensure the scroll pane matches
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
    }

    private JLabel createLabel(String text, int fontSize) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        return lbl;
    }
    
    

    public void setGoalsTab(GoalsTab goalsTab) {
        this.goalsTab = goalsTab;
    }

    public void addTransactionFromOutside(Transaction t) {
        loadFromDatabase();

        if (goalsTab != null) {
            goalsTab.applyTransactionToGoals(t);
        }
    }

    private void refreshAll() {
        tableModel.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Get the current month for filtering
        java.time.YearMonth currentMonth = java.time.YearMonth.now();

        for (Transaction t : transactions) {
            // ONLY add to the table if it belongs to the current month
            if (java.time.YearMonth.from(t.getDate()).equals(currentMonth)) {
                tableModel.addRow(new Object[]{
                    t.getDate().format(df),
                    t.getType(),
                    t.getCategory(),
                    t.getNote(),
                    String.format("₱%.2f", t.getAmount()),
                    "-"
                });
            }
        }

        // This now calculates totals based on the same logic
        updateTotals();

        if (analyticsTab != null) {
            analyticsTab.updateTable();
        }
    }

    private void updateTotals() {
        java.time.YearMonth currentMonth = java.time.YearMonth.now();

        // Calculate Monthly Income
        double income = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                // CRITICAL: Filter for current month and year only
                .filter(t -> java.time.YearMonth.from(t.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate Monthly Expense
        double expense = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                // CRITICAL: Filter for current month and year only
                .filter(t -> java.time.YearMonth.from(t.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double monthlySavings = income - expense;

        balanceLabel.setText(String.format("MONTHLY SAVINGS: ₱%.2f", monthlySavings));
        incomeLabel.setText(String.format("INCOME: ₱%.2f", income));
        expenseLabel.setText(String.format("EXPENSE: ₱%.2f", expense));
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setAnalyticsTab(AnalyticsTab analyticsTab) {
        this.analyticsTab = analyticsTab;
    }

    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("-");
            setFont(new Font("Arial", Font.BOLD, 20));
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private JButton button;
        private int row;

        public ButtonEditor() {
            button = new JButton("-");
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> {
                // 1. Get the transaction object from the list using the row index
                Transaction t = transactions.get(row);

                // 2. Call the DataHandler to delete it from MySQL
                // (Ensure you added the deleteTransaction method to DataHandler first!)
                boolean deletedFromDb = DataHandler.deleteTransaction(t.getTransactionId());

                if (deletedFromDb) {
                    // 3. Only remove from the UI list if the DB delete was successful
                    transactions.remove(row);
                    refreshAll();

                    if (goalsTab != null) {
                        goalsTab.recalculateAllGoals();
                    }
                } else {
                    JOptionPane.showMessageDialog(this.button,
                            "Error: Could not delete from database. Check if Transaction ID is valid.",
                            "Delete Error", JOptionPane.ERROR_MESSAGE);
                }

                fireEditingStopped();
            });
        }

        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int col) {
            this.row = row;
            return button;
        }

        public Object getCellEditorValue() {
            return "-";
        }
    }
    
    public void loadFromDatabase() {
        String userIdStr = AccountManager.getUserId();

        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                this.transactions = DataHandler.loadTransactions(userId);
                refreshAll();
            } catch (NumberFormatException e) {
                System.err.println("Invalid User ID format");
            }
        }
    }
    
    
}