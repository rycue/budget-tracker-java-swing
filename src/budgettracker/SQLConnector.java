package budgettracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {

    private final String ADDRESS = "jdbc:mysql://localhost:3306/jtvi_budget_tracker_db";
    private final String USERNAME = "root";
    private final String PASSWORD = System.getProperty("DB_PASSWORD", "");

    public Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                ADDRESS, USERNAME, PASSWORD);
        return conn;
    }

    public boolean isConnected() {
        boolean isConnect;
        try (Connection connection = createConnection();) {
            isConnect = true;
        } catch (Exception e) {
            isConnect = false;
            System.out.println(e.toString());
        }
        return isConnect;
    }

    public static void main(String[] args) {
        SQLConnector callConnect = new SQLConnector();
        System.out.print("is connected to database? ");
        System.out.println(callConnect.isConnected());
    }
}
