package budgettracker;

import java.time.LocalDate;

public class UserAccount {
    private String fullName;
    private String email;
    private String password;
    private LocalDate dateCreated;
    private String securityQuestion;
    private String securityAnswer;

    public UserAccount(String fullName, String email, String password,
                       String securityQuestion, String securityAnswer) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateCreated = LocalDate.now();
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDate getDateCreated() { return dateCreated; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    
    // Removed getUsername() and getMobile() since they no longer exist
    
    public void setPassword(String newPass) { this.password = newPass; }
}