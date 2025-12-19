package budgettracker;

import java.time.LocalDate;

public class UserAccount {
    private int userID;
    private String fullName;
    private String email;
    private String password;
    private LocalDate dateCreated;
    private String securityQuestion;
    private String securityAnswer;

    // CONSTRUCTOR 1: Used for NEW registrations (No ID yet)
    public UserAccount(String fullName, String email, String password,
            String securityQuestion, String securityAnswer) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateCreated = LocalDate.now();
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // CONSTRUCTOR 2: Used when loading from Database (Has ID)
    public UserAccount(int userID, String fullName, String email, String password,
            String securityQuestion, String securityAnswer) {
        this.userID = userID; // This is the magic fix!
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateCreated = LocalDate.now();
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDate getDateCreated() { return dateCreated; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    
    public void setPassword(String newPass) { this.password = newPass; }
}