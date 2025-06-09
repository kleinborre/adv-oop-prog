package daoimpl;

import dao.SystemManagementDAO;
import db.DatabaseConnection;
import pojo.SystemManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemManagementDAOImpl implements SystemManagementDAO {

    private Connection connection;

    public SystemManagementDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public List<SystemManagement> getAllSystemLogs() throws SQLException {
        List<SystemManagement> systemLogs = new ArrayList<>();
        String query = getBaseQuery();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                systemLogs.add(mapResultSetToSystemManagement(rs));
            }
        }
        return systemLogs;
    }

    @Override
    public List<SystemManagement> getSystemLogsByEmployeeID(int employeeID) throws SQLException {
        List<SystemManagement> systemLogs = new ArrayList<>();
        String query = getBaseQuery() + " WHERE a.employeeID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    systemLogs.add(mapResultSetToSystemManagement(rs));
                }
            }
        }
        return systemLogs;
    }

    @Override
    public List<SystemManagement> getSystemLogsByDate(Date date) throws SQLException {
        List<SystemManagement> systemLogs = new ArrayList<>();
        String query = getBaseQuery() + " WHERE a.date = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    systemLogs.add(mapResultSetToSystemManagement(rs));
                }
            }
        }
        return systemLogs;
    }

    // Helper method to generate base SQL query
    private String getBaseQuery() {
        return "SELECT a.date, a.logIn, a.logOut, a.workedHours, " +
               "e.employeeID, CONCAT(e.firstName, ' ', e.lastName) AS employeeName " +
               "FROM attendance a " +
               "JOIN employee e ON a.employeeID = e.employeeID";
    }

    // Helper method to map ResultSet to SystemManagement POJO
    private SystemManagement mapResultSetToSystemManagement(ResultSet rs) throws SQLException {
        SystemManagement sm = new SystemManagement();
        sm.setEmployeeID(rs.getInt("employeeID"));
        sm.setEmployeeName(rs.getString("employeeName"));
        sm.setDate(rs.getDate("date"));
        sm.setLoginTime(rs.getTime("logIn"));
        sm.setLogoutTime(rs.getTime("logOut"));
        sm.setWorkedHours(rs.getDouble("workedHours"));
        return sm;
    }
}