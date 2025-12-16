package budgettracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsTab extends JPanel {
    private DashboardTab dashboardTab;
    private JTable analyticsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthFilter;
    private JComboBox<String> yearFilter;

    public AnalyticsTab(DashboardTab dashboardTab) {
        this.dashboardTab = dashboardTab;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        // Filters panel
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(Color.BLACK);

        monthFilter = new JComboBox<>();
        monthFilter.addItem("All Months");
        for (Month m : Month.values()) monthFilter.addItem(m.toString());

        yearFilter = new JComboBox<>();
        yearFilter.addItem("All Years");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 1; y++) yearFilter.addItem(String.valueOf(y));

        filterPanel.add(new JLabel("Month:") {{ setForeground(Color.GREEN); }});
        filterPanel.add(monthFilter);
        filterPanel.add(new JLabel("Year:") {{ setForeground(Color.GREEN); }});
        filterPanel.add(yearFilter);

        add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"Date", "Category", "Amount", "Balance", "Result"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        analyticsTable = new JTable(tableModel);
        analyticsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Update table when filters change
        monthFilter.addActionListener(e -> updateTable());
        yearFilter.addActionListener(e -> updateTable());

        updateTable();
    }

    public void updateTable() {
        tableModel.setRowCount(0); 

        List<Transaction> all = dashboardTab.getTransactions();

        String monthSelected = (String) monthFilter.getSelectedItem();
        String yearSelected = (String) yearFilter.getSelectedItem();

        List<Transaction> filtered = all.stream().filter(t -> {
            boolean monthMatch = monthSelected.equals("All Months") || 
                                 t.getDate().getMonth().toString().equals(monthSelected);
            boolean yearMatch = yearSelected.equals("All Years") || 
                                String.valueOf(t.getDate().getYear()).equals(yearSelected);
            return monthMatch && yearMatch;
        }).collect(Collectors.toList());

        // Sort by date
        filtered.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        double runningBalance = 0.0;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Transaction t : filtered) {
            double amount = t.getAmount();
            String amountStr;
            String result;

            if (t.getType() == Transaction.Type.INCOME) {
                runningBalance += amount;
                amountStr = String.format("+₱%.2f", amount);
                result = "Income";
            } else {
                runningBalance -= amount;
                amountStr = String.format("-₱%.2f", amount);
                result = "Expense";
            }

            tableModel.addRow(new Object[]{
                    t.getDate().format(df),
                    t.getCategory(),
                    amountStr,
                    String.format("₱%.2f", runningBalance),
                    result
            });
        }
    }
}