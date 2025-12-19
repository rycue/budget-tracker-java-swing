package budgettracker;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
            LocalDateTime createdAt) {
        System.out.println("New account created at " + createdAt);

        if (email == null || password == null || secretAnswer == null) {
            System.err.println("Registration failed: Required fields cannot be null.");
            return false;
        }

        try {
            String hashedPassword = PasswordHasher.hashPassword(password);
            SQLConnector connector = SQLConnector.getInstance();

            boolean success = connector.insertUser(
                    email,
                    fullName,
                    hashedPassword,
                    secretQuestion,
                    secretAnswer,
                    createdAt
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
    }

    public static String verifyUserLogin(String email, String plainTextPassword) {
        SQLConnector connector = SQLConnector.getInstance();

        try (ResultSet resultSet = connector.getUserByEmail(email)) {

            if (resultSet != null && resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                boolean passwordMatch = PasswordHasher.verifyPassword(plainTextPassword, storedHashedPassword);

                if (passwordMatch) {
                    return resultSet.getString("user_id");
                } else {
                    System.out.println("LOG: Failed login attempt for user " + email + " (Bad Password)");
                    return null;
                }
            } else {
                System.out.println("LOG: Failed login attempt for user " + email + " (User Not Found)");
                return null;
            }
        } catch (java.sql.SQLException e) {
            System.err.println("LOG: DataHandler Error during login: " + e.getMessage());
            return null;
        }
    }

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
            System.err.println("ERROR: Could not load data for user ID: " + userID);
            return null;

        } catch (java.sql.SQLException e) {
            System.err.println("DataHandler Error loading user data: " + e.getMessage());
            return null;
        }
    }

    public static List<Transaction> loadTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.name AS cat_name, c.type AS cat_type "
                + "FROM transactions t "
                + "LEFT JOIN categories c ON t.category_id = c.category_id "
                + "WHERE t.user_id = ? ORDER BY t.created_at DESC";

        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
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
    }

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
        String selectSql = "SELECT category_id FROM categories WHERE name = ? AND user_id = ? AND type = ?";
        String insertSql = "INSERT INTO categories (user_id, name, type) VALUES (?, ?, ?)";
        String typeStr = type.toString().toLowerCase();

        try (Connection conn = SQLConnector.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, name);
                ps.setInt(2, userId);
                ps.setString(3, typeStr);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
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
        String sql = "INSERT INTO goals (user_id, title, target_amount, current_amount, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, goal.getName());
            ps.setDouble(3, goal.getTarget());
            ps.setDouble(4, goal.getProgress());
            ps.setDate(5, java.sql.Date.valueOf(goal.getDateCreated()));

            ps.executeUpdate();

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
                java.sql.Date sqlDate = rs.getDate("created_at");
                LocalDate createdAt = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();

                list.add(new Goal(
                        rs.getInt("goal_id"),
                        rs.getString("title"),
                        rs.getDouble("target_amount"),
                        rs.getDouble("current_amount"),
                        createdAt
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deleteGoal(int goalId, int userId, boolean refund, double amount) {
        try (Connection conn = SQLConnector.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (refund && amount > 0) {
                    int catId = getCategoryIdByName("Goal Refund", userId, Transaction.Type.INCOME);
                    String refundSQL = "INSERT INTO transactions (user_id, category_id, amount, note, created_at) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(refundSQL)) {
                        ps.setInt(1, userId);
                        ps.setInt(2, catId);
                        ps.setDouble(3, amount);
                        ps.setString(4, "Refund from deleted goal");
                        ps.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        ps.executeUpdate();
                    }
                }

                String deleteSQL = "DELETE FROM goals WHERE goal_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                    ps.setInt(1, goalId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fundGoal(int userId, int goalId, double amount, String goalName) {
        try (Connection conn = SQLConnector.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                String updateGoal = "UPDATE goals SET current_amount = current_amount + ? WHERE goal_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateGoal)) {
                    ps.setDouble(1, amount);
                    ps.setInt(2, goalId);
                    ps.executeUpdate();
                }

                int catId = getCategoryIdByName("Goal Savings", userId, Transaction.Type.EXPENSE);
                String insertTrans = "INSERT INTO transactions (user_id, category_id, amount, note, created_at) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertTrans)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, catId);
                    ps.setDouble(3, amount);
                    ps.setString(4, "Deposit to: " + goalName);
                    ps.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateGoalProgress(int goalId, double newProgress) {
        String sql = "UPDATE goals SET current_amount = ? WHERE goal_id = ?";
        try (Connection conn = SQLConnector.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newProgress);
            ps.setInt(2, goalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating goal progress: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteFullAccount(int userId) {
        Connection conn = null;
        try {
            conn = SQLConnector.getInstance().getConnection();
            conn.setAutoCommit(false);

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

            String delUser = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delUser)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            conn.commit();
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
    }

    public static boolean resetUserData(int userId) {
        Connection conn = null;
        try {
            conn = SQLConnector.getInstance().getConnection();
            conn.setAutoCommit(false);

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

            conn.commit();
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
    }

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
        String hashedPass = PasswordHasher.hashPassword(newPlainPassword);
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
