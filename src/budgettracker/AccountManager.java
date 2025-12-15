package budgettracker;

public class AccountManager {
    private static UserAccount loggedInUser = null;
    
    public static void register(UserAccount user) {
        loggedInUser = user;
    }
    
    public static boolean login(String username, String password) {
        return loggedInUser != null &&
               loggedInUser.getUsername().equals(username) &&
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