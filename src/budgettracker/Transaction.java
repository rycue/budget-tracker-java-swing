package budgettracker;

import java.time.LocalDate;

public class Transaction {
    public enum Type { INCOME, EXPENSE }

    private Type type;
    private String category;
    private String note;
    private double amount;

    public Transaction(Type type, String category, String note, double amount) {
        this.type = type;
        this.category = category;
        this.note = note;
        this.amount = amount;
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | ₱%.2f",
                type == Type.INCOME ? "Income" : "Expense",
                category,
                note.isBlank() ? "No note" : note,
                amount);
    }
}
