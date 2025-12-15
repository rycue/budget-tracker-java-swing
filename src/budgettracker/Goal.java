package budgettracker;

public class Goal {

    private String category;
    private double price;
    private String date;
    private String note;

    public Goal(String category, double price, String date, String note) {
        this.category = category;
        this.price = price;
        this.date = date;
        this.note = note;
    }

    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return String.format("%s | $%.2f | %s | %s",
                category,
                price,
                date,
                note.isBlank() ? "No note" : note);
    }
}