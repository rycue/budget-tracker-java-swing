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
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.BLACK);

        balanceLabel = createLabel("BALANCE: ₱0.00", 32);
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(balanceLabel, BorderLayout.NORTH);

        JPanel iePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        iePanel.setBackground(Color.BLACK);

        incomeLabel = createLabel("INCOME: ₱0.00", 24);
        incomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        expenseLabel = createLabel("EXPENSE: ₱0.00", 24);
        expenseLabel.setHorizontalAlignment(SwingConstants.CENTER);

        iePanel.add(incomeLabel);
        iePanel.add(expenseLabel);
        topPanel.add(iePanel, BorderLayout.CENTER);

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
        
    }

    private JLabel createLabel(String text, int fontSize) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.GREEN);
        lbl.setFont(new Font("Arial", Font.BOLD, fontSize));
        return lbl;
    }

    public void setGoalsTab(GoalsTab goalsTab) {
        this.goalsTab = goalsTab;
    }

    public void addTransactionFromOutside(Transaction t) {
        transactions.add(t);
        refreshAll();
   
        if (goalsTab != null) {
            goalsTab.applyTransactionToGoals(t);
        }
    }

    private void refreshAll() {
        tableModel.setRowCount(0);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                    t.getDate().format(df),
                    t.getType(),
                    t.getCategory(),
                    t.getNote(),
                    String.format("₱%.2f", t.getAmount()),
                    "-" // BIG MINUS SIGN AS BUTTON
            });
        }

        updateTotals();

        if (analyticsTab != null) {
            analyticsTab.updateTable();
        }
    }

    private void updateTotals() {
        double income = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expense = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double balance = income - expense;

        balanceLabel.setText(String.format("BALANCE: ₱%.2f", balance));
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
                transactions.remove(row);
                refreshAll();
                
                if (goalsTab != null) {
                    goalsTab.recalculateAllGoals();
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