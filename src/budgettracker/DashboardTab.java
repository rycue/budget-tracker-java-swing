package budgettracker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

public class DashboardTab extends JPanel {

    private TableColumn removeColumn;
    private boolean isEditMode = false;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("MMM. dd");

    private JLabel totalBalanceLabel, savingsLabel, incomeLabel, expenseLabel;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private List<Transaction> transactions;
    private AnalyticsTab analyticsTab;
    private GoalsTab goalsTab;

    // Fields for the Empty State Switcher
    private JPanel tableContainer;
    private CardLayout cl;

    public void toggleEditMode() {
        this.isEditMode = !isEditMode;
        if (isEditMode) {
            try {
                transactionTable.getColumn("Remove");
            } catch (IllegalArgumentException e) {
                transactionTable.getColumnModel().addColumn(removeColumn);
            }
        } else {
            transactionTable.getColumnModel().removeColumn(removeColumn);
        }
        this.revalidate();
        this.repaint();
    }

    public void forceDisableEditMode() {
        if (isEditMode) {
            isEditMode = false;
            transactionTable.getColumnModel().removeColumn(removeColumn);
            this.revalidate();
            this.repaint();
        }
    }

    public DashboardTab() {
        transactions = new ArrayList<>();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(Color.BLACK);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        totalBalanceLabel = new JLabel("₱0.00");
        topPanel.add(createStatCard("TOTAL BALANCE", totalBalanceLabel, 42, new Color(0, 200, 0), true), BorderLayout.NORTH);

        JPanel monthlyContainer = new JPanel(new BorderLayout(0, 15));
        monthlyContainer.setBackground(Color.BLACK);
        monthlyContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "MONTHLY PERFORMANCE", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 10), Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        savingsLabel = new JLabel("₱0.00");
        incomeLabel = new JLabel("₱0.00");
        expenseLabel = new JLabel("₱0.00");

        JPanel ieGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        ieGrid.setBackground(Color.BLACK);
        ieGrid.add(createStatCard("INCOME", incomeLabel, 20, new Color(50, 205, 50), true));
        ieGrid.add(createStatCard("EXPENSE", expenseLabel, 20, new Color(255, 69, 0), true));

        monthlyContainer.add(createStatCard("MONTHLY SAVINGS", savingsLabel, 32, Color.CYAN, true), BorderLayout.NORTH);
        monthlyContainer.add(ieGrid, BorderLayout.CENTER);
        topPanel.add(monthlyContainer, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- HISTORY PANEL ---
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.BLACK);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN, 1),
                "Monthly History", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.GREEN
        ));

        String[] columns = {"Date", "Category", "Note", "Amount", "Remove"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return getColumnName(column).equals("Remove");
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(35);
        transactionTable.setBackground(Color.BLACK);
        transactionTable.setForeground(Color.GREEN);
        transactionTable.setGridColor(Color.DARK_GRAY);
        transactionTable.setSelectionBackground(new Color(0, 100, 0, 100));
        transactionTable.setSelectionForeground(Color.WHITE);

        transactionTable.getTableHeader().setBackground(Color.DARK_GRAY);
        transactionTable.getTableHeader().setForeground(Color.WHITE);
        transactionTable.getTableHeader().setReorderingAllowed(false);

        removeColumn = transactionTable.getColumn("Remove");
        removeColumn.setCellRenderer(new ButtonRenderer());
        removeColumn.setCellEditor(new ButtonEditor());
        transactionTable.getColumnModel().removeColumn(removeColumn);

        transactionTable.setDefaultRenderer(Object.class, new TransactionRenderer());

        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(320);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        // EMPTY STATE IMPLEMENTATION
        cl = new CardLayout();
        tableContainer = new JPanel(cl);
        tableContainer.setBackground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.BLACK);
        JLabel emptyLabel = new JLabel("<html><center><font color='#555555' size='5'>No transactions yet.</font><br>"
                + "<font color='#333333'>Add one using the '+' button below.</font></center></html>");
        placeholder.add(emptyLabel);

        tableContainer.add(scrollPane, "DATA");
        tableContainer.add(placeholder, "EMPTY");

        historyPanel.add(tableContainer, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.CENTER);
    }

    private void refreshAll() {
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        YearMonth currentMonth = YearMonth.now();

        List<Transaction> filtered = transactions.stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .collect(Collectors.toList());

        for (Transaction t : filtered) {
            String dateDisplay;
            if (t.getDate().equals(today)) {
                dateDisplay = "Today";
            } else if (t.getDate().equals(yesterday)) {
                dateDisplay = "Yesterday";
            } else {
                dateDisplay = t.getDate().format(DISPLAY_DATE_FMT);
            }

            tableModel.addRow(new Object[]{
                dateDisplay,
                t.getCategory(),
                t.getNote(),
                t,
                "-"
            });
        }

        // Switch view based on data presence
        if (filtered.isEmpty()) {
            cl.show(tableContainer, "EMPTY");
        } else {
            cl.show(tableContainer, "DATA");
        }

        updateTotals();
    }

    private class TransactionRenderer extends javax.swing.table.DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            if (column == 3 && value instanceof Transaction) {
                Transaction t = (Transaction) value;
                boolean isIncome = t.getType() == Transaction.Type.INCOME;
                String text = (isIncome ? "+ " : "- ") + "₱" + df.format(t.getAmount());

                Component c = super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(isIncome ? new Color(50, 255, 50) : new Color(255, 80, 80));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.BLACK : new Color(20, 20, 20));
                c.setForeground(Color.LIGHT_GRAY);
            } else {
                c.setForeground(Color.WHITE);
            }
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return c;
        }
    }
    public List<Transaction> getTransactions() {
        // Best practice: Never return null for a List, return an empty list instead
        return (transactions != null) ? transactions : new ArrayList<>();
    }

    // --- SUPPORT METHODS ---
    private JPanel createStatCard(String title, JLabel valueLabel, int fontSize, Color defaultColor, boolean hasBorder) {
        JPanel card = new JPanel(new BorderLayout(2, 2));
        card.setBackground(Color.BLACK);
        if (hasBorder) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }
        JLabel header = new JLabel(title);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setForeground(Color.GRAY);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        valueLabel.setForeground(defaultColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(header, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void updateTotals() {
        YearMonth currentMonth = YearMonth.now();
        double monthlyIncome = transactions.stream().filter(t -> t.getType() == Transaction.Type.INCOME && YearMonth.from(t.getDate()).equals(currentMonth)).mapToDouble(Transaction::getAmount).sum();
        double monthlyExpense = transactions.stream().filter(t -> t.getType() == Transaction.Type.EXPENSE && YearMonth.from(t.getDate()).equals(currentMonth)).mapToDouble(Transaction::getAmount).sum();
        double totalIncome = transactions.stream().filter(t -> t.getType() == Transaction.Type.INCOME).mapToDouble(Transaction::getAmount).sum();
        double totalExpense = transactions.stream().filter(t -> t.getType() == Transaction.Type.EXPENSE).mapToDouble(Transaction::getAmount).sum();
        updateUI(totalIncome - totalExpense, monthlyIncome - monthlyExpense, monthlyIncome, monthlyExpense);
    }

    private void updateUI(double balance, double savings, double inc, double exp) {
        totalBalanceLabel.setText("₱" + df.format(balance));
        savingsLabel.setText("₱" + df.format(savings));
        incomeLabel.setText("\u25B2 ₱" + df.format(inc));
        expenseLabel.setText("\u25BC ₱" + df.format(exp));
        totalBalanceLabel.setForeground(balance < 0 ? Color.RED : new Color(0, 180, 0));
        savingsLabel.setForeground(savings < 0 ? Color.ORANGE : Color.CYAN);
    }

    public void loadFromDatabase() {
        String id = AccountManager.getUserId();
        if (id != null && !id.isEmpty()) {
            // Clear old data first to avoid duplicates
            this.transactions = DataHandler.loadTransactions(Integer.parseInt(id));
            refreshAll();

            // Proactively update other tabs if they exist
            if (analyticsTab != null) {
                analyticsTab.updateTable();
            }
            if (goalsTab != null) {
                goalsTab.recalculateAllGoals();
            }
        }
    }
    public void setGoalsTab(GoalsTab g) {
        this.goalsTab = g;
    }

    public void setAnalyticsTab(AnalyticsTab a) {
        this.analyticsTab = a;
    }

    public void addTransactionFromOutside(Transaction t) {
        loadFromDatabase();
        if (goalsTab != null) {
            goalsTab.applyTransactionToGoals(t);
        }
    }

    // --- BUTTON EDITOR/RENDERER ---
    private class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setText("-");
            setBackground(Color.RED);
            setForeground(Color.WHITE);
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return this;
        }
    }

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private JButton button;
        private int currentRow;

        public ButtonEditor() {
            button = new JButton("-");
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                List<Transaction> thisMonth = transactions.stream().filter(t -> YearMonth.from(t.getDate()).equals(YearMonth.now())).collect(Collectors.toList());
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
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.currentRow = r;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "-";
        }
    }
}
