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
import java.time.YearMonth;
import java.util.stream.Collectors;

public class DashboardTab extends JPanel {

    private JLabel totalBalanceLabel;
    private JLabel savingsLabel;
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
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.BLACK);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Container for Balance and Savings
        JPanel mainStatsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        mainStatsPanel.setBackground(Color.BLACK);

        totalBalanceLabel = createLabel("TOTAL BALANCE: ₱0.00", 32);
        totalBalanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainStatsPanel.add(totalBalanceLabel);

        savingsLabel = createLabel("MONTHLY SAVINGS: ₱0.00", 22);
        savingsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainStatsPanel.add(savingsLabel);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        topPanel.add(mainStatsPanel, gbc);

        // Sub-stats (Income & Expense)
        JPanel iePanel = new JPanel(new GridLayout(1, 2, 30, 0));
        iePanel.setBackground(Color.BLACK);

        incomeLabel = createLabel("INCOME: ₱0.00", 18);
        incomeLabel.setForeground(new Color(50, 205, 50));
        incomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        expenseLabel = createLabel("EXPENSE: ₱0.00", 18);
        expenseLabel.setForeground(new Color(255, 69, 0));
        expenseLabel.setHorizontalAlignment(SwingConstants.CENTER);

        iePanel.add(incomeLabel);
        iePanel.add(expenseLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        topPanel.add(iePanel, gbc);

        add(topPanel, BorderLayout.NORTH);

        // HISTORY PANEL
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.BLACK);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN, 2),
                "Monthly History",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.GREEN
        ));

        String[] columns = {"Date", "Type", "Category", "Note", "Amount", "Remove"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(30);
        transactionTable.setBackground(Color.BLACK);
        transactionTable.setForeground(Color.GREEN);
        transactionTable.setGridColor(Color.DARK_GRAY);
        transactionTable.getTableHeader().setBackground(Color.DARK_GRAY);
        transactionTable.getTableHeader().setForeground(Color.WHITE);

        transactionTable.getColumn("Remove").setCellRenderer(new ButtonRenderer());
        transactionTable.getColumn("Remove").setCellEditor(new ButtonEditor());

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        add(historyPanel, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text, int fontSize) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        return lbl;
    }

    public void addTransactionFromOutside(Transaction t) {
        loadFromDatabase(); // Refresh everything from DB
        if (goalsTab != null) {
            goalsTab.applyTransactionToGoals(t);
        }
    }

    private void refreshAll() {
        tableModel.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        YearMonth currentMonth = YearMonth.now();

        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(currentMonth)) {
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
        updateTotals();
        if (analyticsTab != null) {
            analyticsTab.updateTable();
        }
    }

    private void updateTotals() {
        YearMonth currentMonth = YearMonth.now();

        double monthlyIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount).sum();

        double monthlyExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .mapToDouble(Transaction::getAmount).sum();

        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        updateUI(totalIncome - totalExpense, monthlyIncome - monthlyExpense, monthlyIncome, monthlyExpense);
    }

    private void updateUI(double balance, double savings, double inc, double exp) {
        totalBalanceLabel.setText(String.format("TOTAL BALANCE: ₱%.2f", balance));
        savingsLabel.setText(String.format("MONTHLY SAVINGS: ₱%.2f", savings));
        incomeLabel.setText(String.format("INCOME: ₱%.2f", inc));
        expenseLabel.setText(String.format("EXPENSE: ₱%.2f", exp));

        totalBalanceLabel.setForeground(balance < 0 ? Color.RED : new Color(0, 180, 0));
        savingsLabel.setForeground(savings < 0 ? Color.ORANGE : Color.CYAN);
    }

    // --- INNER CLASSES FOR TABLE BUTTONS ---
    private class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setText("-");
            setBackground(Color.RED);
            setForeground(Color.WHITE);
            setBorderPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return this;
        }
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private JButton button;
        private int currentRow;

        public ButtonEditor() {
            button = new JButton("-");
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                // Precision filtering: get ONLY this month's transactions to match table rows
                List<Transaction> thisMonth = transactions.stream()
                        .filter(t -> YearMonth.from(t.getDate()).equals(YearMonth.now()))
                        .collect(Collectors.toList());

                if (currentRow < thisMonth.size()) {
                    Transaction t = thisMonth.get(currentRow);
                    if (DataHandler.deleteTransaction(t.getTransactionId())) {
                        transactions.remove(t);
                        fireEditingStopped();
                        refreshAll();
                        if (goalsTab != null) {
                            goalsTab.recalculateAllGoals();
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            this.currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "-";
        }
    }

    public void loadFromDatabase() {
        String id = AccountManager.getUserId();
        if (id != null) {
            this.transactions = DataHandler.loadTransactions(Integer.parseInt(id));
            refreshAll();
        }
    }

    public void setGoalsTab(GoalsTab g) {
        this.goalsTab = g;
    }

    public void setAnalyticsTab(AnalyticsTab a) {
        this.analyticsTab = a;
    }
}
