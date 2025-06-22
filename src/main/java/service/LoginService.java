package service;

import db.DatabaseConnection;
import pojo.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    // Main login method
    public User login(String usernameOrEmailOrUserID, String password) throws SQLException {

        String query = "SELECT a.userID, a.passwordHash, ur.role, ur.roleID, e.email, e.positionID, " +
                       "CONCAT(e.firstName, ' ', e.lastName) AS username " + // Modify this
                       "FROM authentication a " +
                       "JOIN userrole ur ON a.roleID = ur.roleID " +
                       "LEFT JOIN employee e ON e.userID = a.userID " +
                       "WHERE (a.userID = ? OR e.email = ?) AND a.passwordHash = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, usernameOrEmailOrUserID);
            stmt.setString(2, usernameOrEmailOrUserID);
            stmt.setString(3, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserID(rs.getString("userID"));
                    user.setPassword(rs.getString("passwordHash"));
                    user.setUserRole(rs.getString("role"));
                    user.setEmail(rs.getString("email")); // Can be null if no employee record yet
                    user.setPositionID(rs.getInt("positionID")); // Will be 0 if null
                    user.setUsername(rs.getString("username")); // ← Now correct!

                    return user;
                } else {
                    return null; // Invalid login
                }
            }
        }
    }

    // Get employeeID by userID
    public int getEmployeeIDByUserID(String userID) throws SQLException {
        String query = "SELECT employeeID FROM employee WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("employeeID");
                } else {
                    return -1; // No matching employeeID found
                }
            }
        }
    }
    
    // Check if username/email/userID exists (for better validation messages)
    public boolean doesUserExist(String usernameOrEmailOrUserID) throws SQLException {
        String query = "SELECT 1 FROM authentication a " +
                       "LEFT JOIN employee e ON e.userID = a.userID " +
                       "WHERE a.userID = ? OR e.email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, usernameOrEmailOrUserID);
            stmt.setString(2, usernameOrEmailOrUserID);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if any row exists
            }
        }
    }
}