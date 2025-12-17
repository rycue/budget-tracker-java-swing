package budgettracker;

import java.time.LocalDate;


public class Transaction {

    public enum Type {
        INCOME, EXPENSE
    }

    private int transactionId;
    private int userId;
    private Type type;
    private String category;
    private String note;
    private double amount;
    private LocalDate date;

    public Transaction(int transactionId, Type type, String category, String note, double amount, LocalDate date) {
        this.transactionId = transactionId;
        this.type = type;
        this.category = category;
        this.note = note;
        this.amount = amount;
        this.date = date;
    }

    public Transaction(Type type, String category, String note, double amount) {
        this.type = type;
        this.category = category;
        this.note = note;
        this.amount = amount;
        this.date = LocalDate.now();
    }
  

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
