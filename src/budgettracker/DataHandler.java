
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
                + "LEFT JOIN categories c ON t.category_id = c.category_id "
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
    
    
    
    public static boolean deleteTransaction(int transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete Error: " + e.getMessage());
            return false;
        }
    }
    
    
    public static boolean saveToDatabase(Transaction t, int userId) {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, note, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            // Pass the transaction type so the helper can create the category if it's missing
            ps.setInt(2, getCategoryIdByName(t.getCategory(), userId, t.getType()));
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getNote());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("DB Save Error: " + e.getMessage());
            return false;
        }
    }

    private static int getCategoryIdByName(String name, int userId, Transaction.Type type) {
        // FIX: Match the NAME, the USER, AND the TYPE (income/expense)
        String selectSql = "SELECT category_id FROM categories WHERE name = ? AND user_id = ? AND type = ?";
        String insertSql = "INSERT INTO categories (user_id, name, type) VALUES (?, ?, ?)";

        String typeStr = type.toString().toLowerCase(); // "income" or "expense"

        try (Connection conn = SQLConnector.getInstance().getConnection()) {
            // 1. Look for the exact match
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, name);
                ps.setInt(2, userId);
                ps.setString(3, typeStr);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }

            // 2. If not found, create a NEW row specifically for this type
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setString(3, typeStr);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
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
    
    // ACCOUNT SECTION
    public static boolean deleteFullAccount(int userId) {
        Connection conn = null;
        try {
            conn = SQLConnector.getInstance().getConnection();
            // 1. Disable Auto-Commit to start the transaction block
            conn.setAutoCommit(false);

            // 2. Delete Child Records First (Order matters for Foreign Keys!)
            String delTransactions = "DELETE FROM transactions WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delTransactions)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            String delGoals = "DELETE FROM goals WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delGoals)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            String delCategories = "DELETE FROM categories WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delCategories)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 3. Finally, delete the User
            String delUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delUser)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 4. COMMIT if everything above succeeded
            conn.commit();
            System.out.println("LOG: Account " + userId + " and all data permanently deleted.");
            return true;

        } catch (SQLException e) {
            // 5. ROLLBACK if any error occurred
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("LOG: Deletion failed. Changes rolled back.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            // 6. Restore default behavior and close
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }   
    } // end of  deleteFullAccount()
    
    public static boolean resetUserData(int userId) {
        Connection conn = null;
        try {
            conn = SQLConnector.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Delete all history but keep the user profile
            String delTransactions = "DELETE FROM transactions WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delTransactions)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            String delGoals = "DELETE FROM goals WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delGoals)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            String delCategories = "DELETE FROM categories WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delCategories)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 2. Optional: Reset user balance to 0 in the users table
            String resetBalance = "UPDATE users SET balance = 0 WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(resetBalance)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            conn.commit(); // Success!
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }// end of resetUserData()
    
    // FOR EDIT PROFILE (still under ACCOUNT SECTION)
    public static boolean updateUserName(int userId, String newName) {
        String sql = "UPDATE users SET full_name = ? WHERE user_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserPassword(int userId, String newPlainPassword) {
        // 1. Hash the new password
        String hashedPass = PasswordHasher.hashPassword(newPlainPassword);

        // 2. Now save the HASHED version to the database
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPass);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
}
