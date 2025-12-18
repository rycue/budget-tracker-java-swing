package budgettracker;

import java.time.LocalDate;

public class Goal {
    private int goalID;
    private String name;
    private double target;
    private double progress;
    private LocalDate dateCreated;

    // Constructor for NEW goals (not in DB yet)
    public Goal(String name, double target) {
        this.name = name;
        this.target = target;
        this.progress = 0;
        this.dateCreated = LocalDate.now();
    }
    
    // Constructor for LOADING goals from DB
    public Goal(int goalID, String name, double target, double progress, LocalDate date) {
        this.goalID = goalID;
        this.name = name;
        this.target = target;
        this.progress = progress;
        this.dateCreated = date;
    }
    
    public LocalDate getDateCreated() {
        return dateCreated;
    }
    
    public int getGoalID() {
        return goalID;
    }

    public void setGoalID(int goalID) {
        this.goalID = goalID;
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


    // Add manual progress (from + button)
    public void addManualProgress(double amount) {
        progress += amount;
        if (progress > target) {
            progress = target;
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
