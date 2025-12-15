package budgettracker;

import java.time.LocalDate;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private Type type;
    private String category;
    private String note;
    private double amount;
    private LocalDate date;

    public Transaction(Type type, String category, String note, double amount) {
        this.type = type;
        this.category = category;
        this.note = note;
        this.amount = amount;
        this.date = LocalDate.now(); // transaction date is set automatically
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
