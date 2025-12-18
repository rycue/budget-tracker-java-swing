package budgettracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

public class AnalyticsTab extends JPanel {

    private JPanel cardsPanel;
    private JEditorPane summaryCard; // Single card for plain analysis
    private DashboardTab dashboardTab;
    private JTable analyticsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthFilter;
    private JComboBox<String> yearFilter;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    private boolean isUpdatingFilters = false;

    public AnalyticsTab(DashboardTab dashboardTab) {
        this.dashboardTab = dashboardTab;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);

        // Simple 1-column layout for the single summary card
        cardsPanel = new JPanel(new GridLayout(1, 1));
        cardsPanel.setBackground(Color.BLACK);

        summaryCard = createStyledCard();
        cardsPanel.add(summaryCard);

        monthFilter = new JComboBox<>();
        yearFilter = new JComboBox<>();

        setupUIStructure();

//        String[] columns = {"Date", "Category", "Amount", "Balance", "Type"};
        String[] columns = {"Date", "Category", "Amount", "Balance", "Impact %"};
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
        analyticsTable.setDefaultRenderer(Object.class, new AnalyticsRenderer());

        JScrollPane scrollPane = new JScrollPane(analyticsTable);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);

        monthFilter.addActionListener(e -> updateTable());
        yearFilter.addActionListener(e -> {
            if (!isUpdatingFilters) {
                refreshMonthListOnly();
                updateTable();
            }
        });

        Timer t = new Timer(200, e -> updateTable());
        t.setRepeats(false);
        t.start();
    }

    private JEditorPane createStyledCard() {
        JEditorPane card = new JEditorPane();
        card.setContentType("text/html");
        card.setEditable(false);
        card.setBackground(Color.BLACK);
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 0), 1));
        return card;
    }

    public void updateTable() {
        if (isUpdatingFilters) {
            return;
        }

        List<Transaction> all = (dashboardTab.getTransactions() != null)
                ? dashboardTab.getTransactions() : new ArrayList<>();
        if (all.isEmpty()) {
            return;
        }

        if (yearFilter.getItemCount() == 0) {
            refreshAllFilters(all);
        }

        String monthSelected = (String) monthFilter.getSelectedItem();
        String yearSelected = (String) yearFilter.getSelectedItem();

        // 1. Filter the list
        List<Transaction> filtered = all.stream().filter(t -> {
            boolean mMatch = monthSelected.equals("All Months") || t.getDate().getMonth().toString().equalsIgnoreCase(monthSelected);
            boolean yMatch = yearSelected.equals("All Years") || String.valueOf(t.getDate().getYear()).equals(yearSelected);
            return mMatch && yMatch;
        })
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Reversed: t2 compared to t1
                .collect(Collectors.toList());

        // 2. Calculate total expenses for this filtered view to get the "Impact" base
        double totalMonthlyExpense = filtered.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        tableModel.setRowCount(0);
        double runningBalance = 0.0;

        // Calculate starting balance for the running total
        if (!filtered.isEmpty()) {
            LocalDate firstDate = filtered.get(0).getDate();
            runningBalance = all.stream().filter(t -> t.getDate().isBefore(firstDate))
                    .mapToDouble(t -> t.getType() == Transaction.Type.INCOME ? t.getAmount() : -t.getAmount()).sum();
        }

        for (Transaction t : filtered) {
            runningBalance += (t.getType() == Transaction.Type.INCOME ? t.getAmount() : -t.getAmount());

            // Calculate Impact String
            String impactStr = "---";
            if (t.getType() == Transaction.Type.EXPENSE && totalMonthlyExpense > 0) {
                double impact = (t.getAmount() / totalMonthlyExpense) * 100;
                impactStr = String.format("%.1f%%", impact);
            }

            tableModel.addRow(new Object[]{
                t.getDate().toString(),
                t.getCategory(),
                t, // The transaction object for the renderer
                "₱" + df.format(runningBalance),
                impactStr // Our new "Helper" column
            });
        }

        updateSummaryCard(all, monthSelected, yearSelected);
    }
    
    private void setupUIStructure() {
        // Moved combo boxes to the right side
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterRow.setBackground(Color.BLACK);

        JLabel mLbl = new JLabel("Month:");
        mLbl.setForeground(Color.GREEN);
        JLabel yLbl = new JLabel("Year:");
        yLbl.setForeground(Color.GREEN);

        filterRow.add(mLbl);
        filterRow.add(monthFilter);
        filterRow.add(yLbl);
        filterRow.add(yearFilter);

        JPanel headerGroup = new JPanel(new BorderLayout(0, 10));
        headerGroup.setBackground(Color.BLACK);
        headerGroup.add(filterRow, BorderLayout.NORTH);
        headerGroup.add(cardsPanel, BorderLayout.CENTER);

        add(headerGroup, BorderLayout.NORTH);
    }

    
    private void updateSummaryCard(List<Transaction> all, String monthSelected, String yearSelected) {
        if (monthSelected.equals("All Months")) {
            summaryCard.setText("<html><body style='color:#00FF00; font-family:Monospaced; padding:15px; font-size:16pt;'>&gt; SELECT_SPECIFIC_MONTH</body></html>");
            return;
        }

        Month m = Month.valueOf(monthSelected);

        // Filter based on Month and Year selection
        List<Transaction> monthlyList = all.stream()
                .filter(t -> t.getDate().getMonth().equals(m))
                .filter(t -> yearSelected.equals("All Years") || String.valueOf(t.getDate().getYear()).equals(yearSelected))
                .collect(Collectors.toList());

        double income = monthlyList.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME).mapToDouble(Transaction::getAmount).sum();

        double expense = monthlyList.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE).mapToDouble(Transaction::getAmount).sum();

        // RE-ADDED: Identify the "Culprit" (Top Expense Category)
        String topCategory = monthlyList.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (₱" + df.format(entry.getValue()) + ")")
                .orElse("NONE");

        double net = income - expense;

        String useRateStr;
        if (income > 0) {
            useRateStr = String.format("%.1f%%", (expense / income) * 100);
        } else {
            useRateStr = (expense > 0) ? "OVER_LIMIT" : "0.0%";
        }

        String netColor = (net >= 0) ? "#50FF50" : "#FF5050";
        String rateColor = (income > 0 && (expense / income) > 0.9) ? "#FF5050" : "#50FF50";

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Monospaced; color:#00FF00; padding:20px; font-size:16pt;'>");

        String header = yearSelected.equals("All Years") ? "[ HISTORICAL_AVG: " : "[ MONTHLY_LOG: ";
        html.append("<b style='color:#FFF;'>").append(header).append(monthSelected).append(" ]</b><br><br>");

        html.append("INCOME  : ₱").append(df.format(income)).append("<br>");
        html.append("EXPENSE : ₱").append(df.format(expense)).append("<br>");
        html.append("──────────────────────────────<br>");

        html.append("USE_RATE: <span style='color:").append(rateColor).append(";'>").append(useRateStr).append("</span><br><br>");

        html.append("MONTH_NET:<br>");
        html.append("<span style='font-size:30pt; font-weight:bold; color:").append(netColor).append(";'>₱")
                .append(df.format(net)).append("</span><br><br>");

        // RE-ADDED: The culprit display
        html.append("<span style='font-size:12pt; color:#888;'>MAIN_EXPENSE: ").append(topCategory).append("</span><br><br>");

        String status = (net >= 0) ? "RESULT: POSITIVE_FLOW" : "RESULT: NEGATIVE_FLOW";
        html.append("<div style='border: 2px solid #00FF00; padding: 10px; text-align: center; color:").append(netColor).append(";'>")
                .append("<b>").append(status).append("</b></div>");

        html.append("</body></html>");
        summaryCard.setText(html.toString());
    }
    
    private void refreshAllFilters(List<Transaction> all) {
        isUpdatingFilters = true;
        List<String> years = all.stream().map(t -> String.valueOf(t.getDate().getYear())).distinct().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        yearFilter.removeAllItems();
        if (years.size() > 1) {
            yearFilter.addItem("All Years");
        }
        years.forEach(yearFilter::addItem);
        refreshMonthListOnly();
        isUpdatingFilters = false;
    }

    private void refreshMonthListOnly() {
        List<Transaction> all = dashboardTab.getTransactions();
        if (all == null) {
            return;
        }
        String selectedY = (String) yearFilter.getSelectedItem();
        List<String> months = all.stream().filter(t -> selectedY == null || selectedY.equals("All Years") || String.valueOf(t.getDate().getYear()).equals(selectedY))
                .map(t -> t.getDate().getMonth().toString()).distinct().sorted((m1, m2) -> Month.valueOf(m1).compareTo(Month.valueOf(m2))).collect(Collectors.toList());
        isUpdatingFilters = true;
        monthFilter.removeAllItems();
        if (months.size() > 1) {
            monthFilter.addItem("All Months");
        }
        months.forEach(monthFilter::addItem);
        isUpdatingFilters = false;
    }

    private class AnalyticsRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            // Style the Amount Column (Index 2)
            if (col == 2 && value instanceof Transaction) {
                Transaction t = (Transaction) value;
                boolean isInc = t.getType() == Transaction.Type.INCOME;
                setText((isInc ? "+ ₱" : "- ₱") + df.format(t.getAmount()));
                if (!isSelected) {
                    setForeground(isInc ? new Color(50, 255, 50) : new Color(255, 80, 80));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
            } // Style the Impact Column (Index 4)
            else if (col == 4) {
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    setForeground(Color.GRAY);
                }
            } else {
                setHorizontalAlignment(col == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                if (!isSelected) {
                    setForeground(Color.LIGHT_GRAY);
                }
            }

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.BLACK : new Color(20, 20, 20));
            }
            return c;
        }
    }


}
