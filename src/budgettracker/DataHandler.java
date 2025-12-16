
package budgettracker;

import java.time.LocalDateTime;
import java.sql.ResultSet;


public class DataHandler {
    
    public static boolean registerAccount(
                                    String email, 
                                    String fullName,
                                    String password,
                                    String secretQuestion,
                                    String secretAnswer,
                                    LocalDateTime createdAt,
                                    java.math.BigDecimal balance) {
        System.out.println("RAM: New account created at " + createdAt);
        
        if (email == null || password == null || secretAnswer == null) {
            System.err.println("Registration failed: Required fields cannot be null.");
            return false;
        }
        
        try {
            // DO NOT STORE THE PLAIN TEXT PASSWORD!
            String hashedPassword = PasswordHasher.hashPassword(password);

            // 2. Delegate to the SQLConnector with the HASHED password
            SQLConnector connector = SQLConnector.getInstance();

            boolean success = connector.insertUser(
                    email,
                    fullName,
                    hashedPassword,
                    secretQuestion,
                    secretAnswer,
                    createdAt,
                    balance
            );

            if (success) {
                System.out.println("LOG: Account successfully registered for user.: " + email);
            } else {
                System.err.println("ERROR: Database insertion failed (e.g., duplicate email) for user: " + email);
            }
            return success;

        } catch (Exception e) {
            System.err.println("FATAL ERROR: Registration process failed due to exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    } // end of registerccount()
    
    public static String verifyUserLogin(String email, String plainTextPassword) {
        SQLConnector connector = SQLConnector.getInstance();

        try (ResultSet resultSet = connector.getUserByEmail(email)) {

            if (resultSet != null && resultSet.next()) {
                // User found! Get the stored hashed password.
                String storedHashedPassword = resultSet.getString("password");

                boolean passwordMatch = PasswordHasher.verifyPassword(plainTextPassword, storedHashedPassword);

                if (passwordMatch) {
                    // Login successful. Return the unique user_id (INT) for session management.
                    return resultSet.getString("user_id");
                } else {
                    // Password does not match the hash.
                    System.out.println("LOG: Failed login attempt for user " + email + " (Bad Password)");
                    return null;
                }
            } else {
                // User not found in the database.
                System.out.println("LOG: Failed login attempt for user " + email + " (User Not Found)");
                return null;
            }
        } catch (java.sql.SQLException e) {
            System.err.println("DataHandler Error during login: " + e.getMessage());
            return null;
        }
    } // end of vertifyUserLogin()
    
    /**
     * Loads a full UserAccount object from the database using the user's ID.
     *
     * @param userID The unique ID of the successfully logged-in user.
     * @return A populated UserAccount object, or null if the user data could
     * not be found.
     */
    public static UserAccount loadUserAccount(String userID) {
        SQLConnector connector = SQLConnector.getInstance();

        try (java.sql.ResultSet resultSet = connector.getUserByID(userID)) {

            if (resultSet != null && resultSet.next()) {
                // Retrieve all data fields
                String fullName = resultSet.getString("full_name");
                String email = resultSet.getString("email");
                // NOTE: We do not retrieve the password hash here!
                String secretQuestion = resultSet.getString("secret_question");
                String secretAnswer = resultSet.getString("secret_answer");

                // Create the UserAccount object
                UserAccount account = new UserAccount(
                        fullName,
                        email,
                        "", // Password field is left empty or null for security reasons
                        secretQuestion,
                        secretAnswer
                );

                // account.setUserID(userID);
                return account;
            }
            // If resultSet.next() is false, the user was not found, or the ResultSet was null
            System.err.println("ERROR: Could not load data for user ID: " + userID);
            return null;

        } catch (java.sql.SQLException e) {
            System.err.println("DataHandler Error loading user data: " + e.getMessage());
            return null;
        }
    } // end of loadUserAccount()
    
}
