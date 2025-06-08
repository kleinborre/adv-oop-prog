package daoimpl;

import dao.EmployeeDAO;
import db.DatabaseConnection;
import pojo.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl implements EmployeeDAO {

    private Connection connection;

    public EmployeeDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Employee getEmployeeByID(int employeeID) throws SQLException {
        String query = "SELECT * FROM employee WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Employee getEmployeeByUserID(String userID) throws SQLException {
        String query = "SELECT * FROM employee WHERE userID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employeeList = new ArrayList<>();
        String query = "SELECT * FROM employee";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                employeeList.add(mapResultSetToEmployee(rs));
            }
        }
        return employeeList;
    }

    @Override
    public void addEmployee(Employee employee) throws SQLException {
        String query = "INSERT INTO employee (firstName, lastName, birthDate, phoneNo, email, userID, statusID, positionID, departmentID, supervisorID) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setDate(3, employee.getBirthDate());
            stmt.setString(4, employee.getPhoneNo());
            stmt.setString(5, employee.getEmail());
            stmt.setString(6, employee.getUserID());
            stmt.setInt(7, employee.getStatusID());
            stmt.setInt(8, employee.getPositionID());
            stmt.setInt(9, employee.getDepartmentID());
            stmt.setInt(10, employee.getSupervisorID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateEmployee(Employee employee) throws SQLException {
        String query = "UPDATE employee SET firstName = ?, lastName = ?, birthDate = ?, phoneNo = ?, email = ?, " +
                       "statusID = ?, positionID = ?, departmentID = ?, supervisorID = ? WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setDate(3, employee.getBirthDate());
            stmt.setString(4, employee.getPhoneNo());
            stmt.setString(5, employee.getEmail());
            stmt.setInt(6, employee.getStatusID());
            stmt.setInt(7, employee.getPositionID());
            stmt.setInt(8, employee.getDepartmentID());
            stmt.setInt(9, employee.getSupervisorID());
            stmt.setInt(10, employee.getEmployeeID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteEmployee(int employeeID) throws SQLException {
        String query = "DELETE FROM employee WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Employee POJO
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeID(rs.getInt("employeeID"));
        employee.setFirstName(rs.getString("firstName"));
        employee.setLastName(rs.getString("lastName"));
        employee.setBirthDate(rs.getDate("birthDate"));
        employee.setPhoneNo(rs.getString("phoneNo"));
        employee.setEmail(rs.getString("email"));
        employee.setUserID(rs.getString("userID"));
        employee.setStatusID(rs.getInt("statusID"));
        employee.setPositionID(rs.getInt("positionID"));
        employee.setDepartmentID(rs.getInt("departmentID"));
        employee.setSupervisorID(rs.getInt("supervisorID"));
        return employee;
    }
}