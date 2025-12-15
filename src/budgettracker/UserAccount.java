package budgettracker;

import java.time.LocalDate;

public class UserAccount {
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String username;
    private String password;
    private LocalDate dateCreated;

    private String securityQuestion;
    private String securityAnswer; 

    public UserAccount(String firstName, String lastName, String mobile,
                       String email, String username, String password,
                       String securityQuestion, String securityAnswer) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.email = email;
        this.username = username;
        this.password = password;
        this.dateCreated = LocalDate.now();

        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public String getFullName() { return firstName + " " + lastName; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public LocalDate getDateCreated() { return dateCreated; }

    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }

    public void setPassword(String newPass) { this.password = newPass; } 
}
