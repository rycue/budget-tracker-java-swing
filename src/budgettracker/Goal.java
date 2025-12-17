package budgettracker;

import java.time.LocalDate;

public class Goal {

    private String name;
    private double target;
    private double progress;
    private LocalDate dateCreated;

    public Goal(String name, double target) {
        this.name = name;
        this.target = target;
        this.progress = 0;
        this.dateCreated = LocalDate.now();
    }

    public String getName() {
        return name;
    }

    public double getTarget() {
        return target;
    }

    public double getProgress() {
        return progress;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    // Add manual progress (from + button)
    public void addManualProgress(double amount) {
        progress += amount;
        if (progress > target) {
            progress = target;
        }
    }

    public void applyTransaction(Transaction t) {
        if (t.getType() == Transaction.Type.INCOME || t.getType() == Transaction.Type.EXPENSE) {
            progress += t.getAmount();
            if (progress > target) {
                progress = target;
            }
        }
    }

    // Check if goal is completed
    public boolean isCompleted() {
        return progress >= target;
    }

    // Reset goal progress
    public void reset() {
        progress = 0;
    }
}
