package service;

import dao.EmployeeDAO;
import daoimpl.EmployeeDAOImpl;
import pojo.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import db.DatabaseConnection;
import java.util.List;

public class EmployeeService {

    private EmployeeDAO employeeDAO;

    public EmployeeService() {
        try {
            employeeDAO = new EmployeeDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing EmployeeDAO", e);
        }
    }

    public Employee getEmployeeByID(int employeeID) {
        try {
            // CUSTOMIZED → add JOIN to get position name
            String query = "SELECT e.*, p.position " +
                           "FROM employee e " +
                           "JOIN position p ON e.positionID = p.positionID " +
                           "WHERE e.employeeID = ?";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, employeeID);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Employee employee = new Employee();
                        employee.setEmployeeID(rs.getInt("employeeID"));
                        employee.setFirstName(rs.getString("firstName"));
                        employee.setLastName(rs.getString("lastName"));
                        employee.setPositionID(rs.getInt("positionID"));
                        employee.setPosition(rs.getString("position")); // NEW FIELD
                        return employee;
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee by ID", e);
        }
    }

    public Employee getEmployeeByUserID(String userID) {
        try {
            return employeeDAO.getEmployeeByUserID(userID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee by userID", e);
        }
    }

    public List<Employee> getAllEmployees() {
        try {
            return employeeDAO.getAllEmployees();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all employees", e);
        }
    }

    public void addEmployee(Employee employee) {
        try {
            employeeDAO.addEmployee(employee);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding employee", e);
        }
    }

    public void updateEmployee(Employee employee) {
        try {
            employeeDAO.updateEmployee(employee);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee", e);
        }
    }

    public void deleteEmployee(int employeeID) {
        try {
            employeeDAO.deleteEmployee(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting employee", e);
        }
    }
}