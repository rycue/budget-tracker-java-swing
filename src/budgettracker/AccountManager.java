package budgettracker;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountManager {
    private static UserAccount loggedInUser = null;
    private static String userId;
    
    public static boolean register(UserAccount user) {
        
        String email = user.getEmail();
        String fullName = user.getFullName();
        String plainTextPassword = user.getPassword();
        String secretQuestion = user.getSecurityQuestion();
        String secretAnswer = user.getSecurityAnswer();
        LocalDateTime registrationTime = LocalDateTime.now();
        BigDecimal startingBalance = new BigDecimal(0.00);
        
        boolean registrationSuccess = DataHandler.registerAccount(
                email,
                fullName,
                plainTextPassword,
                secretQuestion,
                secretAnswer,
                registrationTime,
                startingBalance
        );
        
        if (registrationSuccess) {
            String realID = DataHandler.verifyUserLogin(email, plainTextPassword);

            if (realID != null) {
                loggedInUser = DataHandler.loadUserAccount(realID);
                return true; // Path 1: Everything worked
            }
            // If we are here, registration worked but login fetch failed.
            return false; // Path 2: Fail because we couldn't get the ID
        } else {
            return false; // Path 3: Registration failed
        }
    }
    
    public static boolean loginWithEmail(String email, String password) {

        String userID = DataHandler.verifyUserLogin(email, password);

        if (userID != null) {
            UserAccount user = DataHandler.loadUserAccount(userID);

            if (user != null) {
                loggedInUser = user;
                System.out.println("LOG: User " + email + " successfully logged in.");
                return true;
            }
        }

        System.err.println("ERROR: Login failed for user " + email);
        return false;
    }
    
    public static UserAccount getUser() {
        return loggedInUser;
    }
    
    public static void setUserId(String id) {
        userId = id;
    }
    
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }
    
    public static void logout() {
        if (loggedInUser != null) {
            String email = loggedInUser.getEmail();
            loggedInUser = null;
            System.out.println("LOG: User " + email + " successfully logged out.");
        } else {
            System.out.println("LOG: Logout called, but no user was logged in.");
        }
    }
    
    public static String getUserId() {
        // Check if loggedInUser is not null, then get its ID
        return (loggedInUser != null) ? String.valueOf(loggedInUser.getUserID()) : null;
    }
}