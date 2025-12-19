package budgettracker;

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
        
        boolean registrationSuccess = DataHandler.registerAccount(
                email,
                fullName,
                plainTextPassword,
                secretQuestion,
                secretAnswer,
                registrationTime
        );
        
        if (registrationSuccess) {
            String realID = DataHandler.verifyUserLogin(email, plainTextPassword);

            if (realID != null) {
                loggedInUser = DataHandler.loadUserAccount(realID);
                return true;
            }
            return false;
        } else {
            return false;
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
    
    // FOR ACCOUNT SETTINGS
    public static void updateLocalUser(String newName) {
        if (loggedInUser != null) {
            loggedInUser.setFullName(newName);
        }
    }

    public static void updateLocalPassword(String newPass) {
        if (loggedInUser != null) {
            loggedInUser.setPassword(newPass);
        }
    }
    
    public static boolean resetUserPassword(int userID, String plainTextPassword) {
        String newHash = PasswordHasher.hashPassword(plainTextPassword);

        boolean success = SQLConnector.getInstance().updatePassword(userID, newHash);

        if (success && loggedInUser != null && loggedInUser.getUserID() == userID) {
            loggedInUser.setPassword(newHash);
        }

        return success;
    }
    
    public static boolean resetPasswordExternal(UserAccount user, String newPlainTextPassword) {
        String hashed = PasswordHasher.hashPassword(newPlainTextPassword);

        boolean success = SQLConnector.getInstance().updatePassword(user.getUserID(), hashed);

        if (success && loggedInUser != null && loggedInUser.getUserID() == user.getUserID()) {
            loggedInUser.setPassword(hashed);
        }
        return success;
    }
}