package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static DatabaseConnection instance;
    private final Connection connection;

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error establishing database connection", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        try {
            if (instance == null || instance.connection.isClosed()) {
                instance = new DatabaseConnection();
            }
            return instance;
        } catch (SQLException e) {
            throw new RuntimeException("Error re-initializing database connection", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}