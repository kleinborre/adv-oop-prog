package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // MySQL Connection details;
    private static final String URL = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    
    // Get a Connection Object
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
