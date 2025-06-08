package daoimpl;

import dao.UserDAO;
import db.DatabaseConnection;
import pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private final Connection connection;

    public UserDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public User getUserByUserID(String userID) throws SQLException {
        String query = "SELECT a.userID, CONCAT(e.firstName, ' ', e.lastName) AS username, " +
                       "e.email, a.passwordHash, ur.role, e.positionID " +
                       "FROM authentication a " +
                       "JOIN userrole ur ON a.roleID = ur.roleID " +
                       "JOIN employee e ON a.userID = e.userID " +
                       "WHERE a.userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT a.userID, CONCAT(e.firstName, ' ', e.lastName) AS username, " +
                       "e.email, a.passwordHash, ur.role, e.positionID " +
                       "FROM authentication a " +
                       "JOIN userrole ur ON a.roleID = ur.roleID " +
                       "JOIN employee e ON a.userID = e.userID " +
                       "WHERE e.email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT a.userID, CONCAT(e.firstName, ' ', e.lastName) AS username, " +
                       "e.email, a.passwordHash, ur.role, e.positionID " +
                       "FROM authentication a " +
                       "JOIN userrole ur ON a.roleID = ur.roleID " +
                       "JOIN employee e ON a.userID = e.userID " +
                       "WHERE CONCAT(e.firstName, ' ', e.lastName) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT a.userID, CONCAT(e.firstName, ' ', e.lastName) AS username, " +
                       "e.email, a.passwordHash, ur.role, e.positionID " +
                       "FROM authentication a " +
                       "JOIN userrole ur ON a.roleID = ur.roleID " +
                       "JOIN employee e ON a.userID = e.userID";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void addUser(User user) throws SQLException {
        // Insert into authentication first
        String authQuery = "INSERT INTO authentication (userID, passwordHash, roleID) " +
                           "VALUES (?, ?, (SELECT roleID FROM userrole WHERE role = ?))";
        try (PreparedStatement stmt = connection.prepareStatement(authQuery)) {
            stmt.setString(1, user.getUserID());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getUserRole());
            stmt.executeUpdate();
        }

        // Insert into employee second
        String empQuery = "INSERT INTO employee (userID, email, firstName, lastName, positionID, departmentID, statusID, birthDate, phoneNo, compensationID, supervisorID) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, '000-000-000', 1, 10001)";
        try (PreparedStatement stmt = connection.prepareStatement(empQuery)) {
            stmt.setString(1, user.getUserID());
            stmt.setString(2, user.getEmail());
            String[] nameParts = user.getUsername().split(" ", 2);
            stmt.setString(3, nameParts.length > 0 ? nameParts[0] : "");
            stmt.setString(4, nameParts.length > 1 ? nameParts[1] : "");
            stmt.setInt(5, user.getPositionID());
            stmt.setInt(6, 1); // Default departmentID
            stmt.setInt(7, 1); // Default statusID (Regular)
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateUser(User user) throws SQLException {
        String authQuery = "UPDATE authentication a " +
                           "JOIN userrole ur ON a.roleID = ur.roleID " +
                           "SET a.passwordHash = ?, a.roleID = (SELECT roleID FROM userrole WHERE role = ?) " +
                           "WHERE a.userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(authQuery)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getUserRole());
            stmt.setString(3, user.getUserID());
            stmt.executeUpdate();
        }

        String empQuery = "UPDATE employee SET email = ?, positionID = ? " +
                          "WHERE userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(empQuery)) {
            stmt.setString(1, user.getEmail());
            stmt.setInt(2, user.getPositionID());
            stmt.setString(3, user.getUserID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteUser(String userID) throws SQLException {
        String query = "DELETE FROM authentication WHERE userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to User POJO
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getString("userID"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("passwordHash"));
        user.setUserRole(rs.getString("role"));
        user.setPositionID(rs.getInt("positionID"));
        return user;
    }
}