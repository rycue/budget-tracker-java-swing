package budgettracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsTab extends JPanel {

    private DashboardTab dashboardTab;
    private JTable statsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> periodComboBox;

    public AnalyticsTab(DashboardTab dashboardTab) {
        this.dashboardTab = dashboardTab;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.BLACK);
        topPanel.add(new JLabel("Select Period: ") {
            {
                setForeground(Color.GREEN);
            }
        });
        periodComboBox = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        periodComboBox.setBackground(Color.GREEN);
        periodComboBox.setForeground(Color.BLACK);
        topPanel.add(periodComboBox);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"When", "Balance", "Change", "Result (%)"};
        
        tableModel = new DefaultTableModel(columns, 0);
        statsTable = new JTable(tableModel);
        statsTable.setBackground(Color.BLACK);
        statsTable.setForeground(Color.GREEN);
        statsTable.setGridColor(Color.GREEN);
        statsTable.getTableHeader().setBackground(Color.BLACK);
        statsTable.getTableHeader().setForeground(Color.GREEN);
        add(new JScrollPane(statsTable), BorderLayout.CENTER);

        periodComboBox.addActionListener(e -> updateStats());
        updateStats();
    }

    private void updateStats() {
        tableModel.setRowCount(0);
        if (dashboardTab == null) {
            return;
        }

        List<Transaction> transactions = dashboardTab.getTransactions();
        Map<String, Double> periodBalances = new LinkedHashMap<>();

        // Group transactions by month
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (Transaction t : transactions) {
            LocalDate date = t.getDate();
            String monthKey = date.format(monthFormatter);

            double total = 0;
            int index = 1;
            String period = (String) periodComboBox.getSelectedItem();

            total += t.getType() == Transaction.Type.INCOME ? t.getAmount() : -t.getAmount();
            String key = switch (period) {
                case "Weekly" ->
                    "Week " + index;
                case "Monthly" ->
                    "Month " + index;
                default ->
                    "Year " + index;
            };
            periodBalances.put(key, total);
            index++;

            double prev = 0;
            for (Map.Entry<String, Double> entry : periodBalances.entrySet()) {
                double balance = entry.getValue();
                double change = balance - prev;
                double result = prev != 0 ? (change / prev) * 100 : 0;
                tableModel.addRow(new Object[]{
                    entry.getKey(),
                    String.format("₱%.2f", balance),
                    String.format("%+.2f", change),
                    String.format("%+.2f%%", result)
                });
                prev = balance;
            }
            
        }
    }
