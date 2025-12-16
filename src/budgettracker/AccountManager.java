package budgettracker;

public class AccountManager {
    private static UserAccount loggedInUser = null;
    
    public static void register(UserAccount user) {
        loggedInUser = user;
    }
    
    public static boolean loginWithEmail(String email, String password) {
        return loggedInUser != null &&
               loggedInUser.getEmail().equals(email) &&
               loggedInUser.getPassword().equals(password);
    }
    
    public static UserAccount getUser() {
        return loggedInUser;
    }
    
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }
    
    public static void logout() {
        loggedInUser = null;
    }
}