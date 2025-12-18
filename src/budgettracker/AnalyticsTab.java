package budgettracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

public class AnalyticsTab extends JPanel {

    private DashboardTab dashboardTab;
    private JTable analyticsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthFilter;
    private JComboBox<String> yearFilter;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public AnalyticsTab(DashboardTab dashboardTab) {
        this.dashboardTab = dashboardTab;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        // --- FILTERS PANEL ---
        JPanel filterPanel = new JPanel();
        filterPanel.setBackground(Color.BLACK);

        monthFilter = new JComboBox<>();
        monthFilter.addItem("All Months");
        for (Month m : Month.values()) {
            monthFilter.addItem(m.toString());
        }

        yearFilter = new JComboBox<>();
        yearFilter.addItem("All Years");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear + 1; y++) {
            yearFilter.addItem(String.valueOf(y));
        }

        filterPanel.add(new JLabel("Month:") {
            {
                setForeground(Color.GREEN);
            }
        });
        filterPanel.add(monthFilter);
        filterPanel.add(new JLabel("Year:") {
            {
                setForeground(Color.GREEN);
            }
        });
        filterPanel.add(yearFilter);

        add(filterPanel, BorderLayout.NORTH);

        // --- TABLE SETUP ---
        String[] columns = {"Date", "Category", "Amount", "Balance", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        analyticsTable = new JTable(tableModel);
        analyticsTable.setRowHeight(30);
        analyticsTable.setBackground(Color.BLACK);
        analyticsTable.setForeground(Color.LIGHT_GRAY);
        analyticsTable.setGridColor(Color.DARK_GRAY);

        // FIX 1: Sync the selection colors with DashboardTab
        analyticsTable.setSelectionBackground(new Color(0, 100, 0, 100));
        analyticsTable.setSelectionForeground(Color.WHITE);

        analyticsTable.getTableHeader().setBackground(Color.DARK_GRAY);
        analyticsTable.getTableHeader().setForeground(Color.WHITE);
        analyticsTable.getTableHeader().setReorderingAllowed(false);

        // FIX 2: Apply the custom renderer for signs and colors
        analyticsTable.setDefaultRenderer(Object.class, new AnalyticsRenderer());

        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        add(scrollPane, BorderLayout.CENTER);

        monthFilter.addActionListener(e -> updateTable());
        yearFilter.addActionListener(e -> updateTable());

        // FIX 3: Use a Timer to delay the initial load 
        // This ensures the DashboardTab has finished its DB load before we pull data
        Timer initialLoadTimer = new Timer(100, e -> updateTable());
        initialLoadTimer.setRepeats(false);
        initialLoadTimer.start();
    }

    public void updateTable() {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);

        // Safeguard: Use the fixed getter we discussed earlier
        List<Transaction> all = (dashboardTab.getTransactions() != null)
                ? dashboardTab.getTransactions()
                : new ArrayList<>();

        if (all.isEmpty()) {
            return;
        }

        String monthSelected = (String) monthFilter.getSelectedItem();
        String yearSelected = (String) yearFilter.getSelectedItem();

        List<Transaction> filtered = all.stream().filter(t -> {
            boolean monthMatch = monthSelected.equals("All Months")
                    || t.getDate().getMonth().toString().equalsIgnoreCase(monthSelected);
            boolean yearMatch = yearSelected.equals("All Years")
                    || String.valueOf(t.getDate().getYear()).equals(yearSelected);
            return monthMatch && yearMatch;
        }).sorted((t1, t2) -> t1.getDate().compareTo(t2.getDate()))
                .collect(Collectors.toList());

        double runningBalance = 0.0;
        if (!filtered.isEmpty()) {
            LocalDate firstDate = filtered.get(0).getDate();
            runningBalance = all.stream()
                    .filter(t -> t.getDate().isBefore(firstDate))
                    .mapToDouble(t -> t.getType() == Transaction.Type.INCOME ? t.getAmount() : -t.getAmount())
                    .sum();
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Transaction t : filtered) {
            boolean isIncome = t.getType() == Transaction.Type.INCOME;
            runningBalance += isIncome ? t.getAmount() : -t.getAmount();

            tableModel.addRow(new Object[]{
                t.getDate().format(dateFmt),
                t.getCategory(),
                t, // Pass the whole object for the renderer
                "₱" + df.format(runningBalance),
                t.getType().toString()
            });
        }
    }

    private class AnalyticsRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            // Amount Column Logic (Index 2)
            if (column == 2 && value instanceof Transaction) {
                Transaction t = (Transaction) value;
                boolean isInc = t.getType() == Transaction.Type.INCOME;
                String text = (isInc ? "+ ₱" : "- ₱") + df.format(t.getAmount());

                Component c = super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(isInc ? new Color(50, 255, 50) : new Color(255, 80, 80));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Apply zebra striping and selection logic
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.BLACK : new Color(20, 20, 20));
                c.setForeground(Color.LIGHT_GRAY);
            } else {
                c.setForeground(Color.WHITE);
            }

            setHorizontalAlignment(column == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return c;
        }
    }
}
