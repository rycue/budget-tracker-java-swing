package budgettracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.ResultSet;

public class SQLConnector {
    
    private static SQLConnector instance;
    
    private final String ADDRESS = "jdbc:mysql://localhost:3306/jtvi_budget_tracker_db";
    private final String USERNAME = "root";
    private final String PASSWORD = System.getProperty("DB_PASSWORD", "");
    
    private SQLConnector() {
        System.out.println("SQLConnector initialized.");
    }
    
    public static SQLConnector getInstance() {
        if (instance == null) {
            instance = new SQLConnector();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                ADDRESS, USERNAME, PASSWORD);
        return conn;
    }
    
    public boolean isConnected() {
        boolean isConnect;
        try (Connection connection = getConnection();) {
            isConnect = true;
        } catch (Exception e) {
            isConnect = false;
            System.out.println(e.toString());
        }
        return isConnect;
    }
    
    public boolean insertUser(
            String email,
            String fullName,
            String hashedPassword,
            String secretQuestion,
            String secretAnswer,
            java.time.LocalDateTime createdAt,
            java.math.BigDecimal balance
    ) {
        String sql = "INSERT INTO users (full_name, email, password, secret_question, secret_answer, created_at, balance) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.setString(4, secretQuestion);
            ps.setString(5, secretAnswer);
            Timestamp dbTimestamp = Timestamp.valueOf(createdAt);
            ps.setTimestamp(6, dbTimestamp);
            ps.setBigDecimal(7, balance);
            
            // If rowsAffected is 1, the insertion was successful
            int rowAffected = ps.executeUpdate();
            return rowAffected == 1;
        } catch (Exception e) {
            System.err.println("SQLConnector Error - Failed to insert user: " + e.getMessage());
            return false;
        }
    }
    
    public ResultSet getUserByEmail(String email) {
        String sql = "SELECT user_id, email, password, secret_question, secret_answer, balance FROM users WHERE email = ?";
        
        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            
            ps.setString(1, email);
            
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("SQLConnector Error - Failed to fetch user by email: " + e.getMessage());
            return null;
        }
    }
    
    public ResultSet getUserByID(String userID) {
        String sql = "SELECT user_id, full_name, email, secret_question, secret_answer, balance FROM users WHERE user_id = ?";

        try {
            java.sql.Connection connection = getConnection();
            java.sql.PreparedStatement ps = connection.prepareStatement(sql);

            // Since we are passing the ID as a String from DataHandler, use setString
            ps.setString(1, userID);

            return ps.executeQuery();

        } catch (java.sql.SQLException e) {
            System.err.println("SQLConnector Error - Failed to fetch user by ID: " + e.getMessage());
            return null;
        }
    } // end of getUserByID()
    
    
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        SQLConnector callConnect = new SQLConnector();
        System.out.print("is connected to database? ");
        System.out.println(callConnect.isConnected());
    }
   
}
