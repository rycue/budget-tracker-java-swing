
package budgettracker;

import java.time.LocalDateTime;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


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

                int numericID = Integer.parseInt(userID);

                String fullName = resultSet.getString("full_name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String secretQuestion = resultSet.getString("secret_question");
                String secretAnswer = resultSet.getString("secret_answer");

                // 2. PASS numericID into the constructor so the object "knows" who it is
                UserAccount account = new UserAccount(
                        numericID,
                        fullName,
                        email,
                        password,
                        secretQuestion,
                        secretAnswer
                );

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
    
    public static List<Transaction> loadTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        // Use an INNER JOIN to get the Category Name and Type from the categories table
        String sql = "SELECT t.*, c.name AS cat_name, c.type AS cat_type "
                + "FROM transactions t "
                + "JOIN categories c ON t.category_id = c.category_id "
                + "WHERE t.user_id = ? ORDER BY t.created_at DESC";

        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Determine if it's INCOME or EXPENSE based on the category table
                String typeStr = rs.getString("cat_type");
                Transaction.Type type = typeStr.equalsIgnoreCase("INCOME")
                        ? Transaction.Type.INCOME : Transaction.Type.EXPENSE;

                list.add(new Transaction(
                        rs.getInt("transaction_id"),
                        type, 
                        rs.getString("cat_name"),
                        rs.getString("note"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("created_at").toLocalDateTime().toLocalDate()
                ));
            }
        } catch (Exception e) {
            System.err.println("DataHandler Load Error: " + e.getMessage());
        }
        return list;
    } // end of loadTransactions()
    
    public static boolean saveToDatabase(Transaction t, int userId) {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, note, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // Translate "Food" -> ID
            ps.setInt(2, getCategoryIdByName(t.getCategory(), userId));
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getNote());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("DB Save Error: " + e.getMessage());
            return false;
        }
    }

    // Helper to find the ID from 'categories' table
    private static int getCategoryIdByName(String name, int userId) {
        String sql = "SELECT category_id FROM categories WHERE name = ? AND user_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("category_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default to first category if not found
    }
    
    
    // GOAL SECTION
    public static int saveGoal(Goal goal, int userId) {
        String sql = "INSERT INTO goals (user_id, title, target_amount, current_amount, deadline) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, goal.getName());
            ps.setDouble(3, goal.getTarget());
            ps.setDouble(4, goal.getProgress());
            ps.setDate(5, java.sql.Date.valueOf(goal.getDateCreated()));

            ps.executeUpdate();

            // Get the ID generated by MySQL and return it
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static ArrayList<Goal> loadGoals(int userId) {
        ArrayList<Goal> list = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Goal(
                        rs.getInt("goal_id"), // From column: goal_id
                        rs.getString("title"), // From column: title
                        rs.getDouble("target_amount"), // From column: target_amount
                        rs.getDouble("current_amount"),// From column: current_amount
                        rs.getDate("deadline").toLocalDate() // From column: deadline
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static boolean deleteGoal(int goalId) {
        String sql = "DELETE FROM goals WHERE goal_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, goalId);
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean updateGoalProgress(int goalId, double newProgress) {
        String sql = "UPDATE goals SET current_amount = ? WHERE goal_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newProgress);
            ps.setInt(2, goalId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating goal progress: " + e.getMessage());
            return false;
        }
    }
    
    
}
