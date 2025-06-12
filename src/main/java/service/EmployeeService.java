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
            // COMPOSITE QUERY — get position, status, govid, address
            String query = "SELECT e.*, " +
                           "p.position, " +
                           "s.statusType AS statusDesc, " +
                           "g.sss AS sssNo, " +
                           "g.pagibig AS pagibigNo, " +
                           "g.philhealth AS philhealthNo, " +
                           "g.tin AS tinNo, " +
                           "CONCAT(a.houseNo, ' ', a.street, ', ', a.barangay, ', ', a.city, ', ', a.province, ', ', a.zipCode) AS fullAddress " +
                           "FROM employee e " +
                           "JOIN position p ON e.positionID = p.positionID " +
                           "JOIN status s ON e.statusID = s.statusID " +
                           "JOIN govid g ON e.employeeID = g.employeeID " + // <-- FIXED THIS LINE
                           "JOIN employeeaddress ea ON e.employeeID = ea.employeeID " +
                           "JOIN address a ON ea.addressID = a.addressID " +
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
                        employee.setBirthDate(rs.getDate("birthDate"));
                        employee.setPhoneNo(rs.getString("phoneNo"));
                        employee.setEmail(rs.getString("email"));
                        employee.setUserID(rs.getString("userID"));
                        employee.setStatusID(rs.getInt("statusID"));
                        employee.setPositionID(rs.getInt("positionID"));
                        employee.setDepartmentID(rs.getInt("departmentID"));
                        employee.setSupervisorID(rs.getInt("supervisorID"));

                        // Transient fields
                        employee.setPosition(rs.getString("position"));
                        employee.setStatusDesc(rs.getString("statusDesc"));
                        employee.setSssNo(rs.getString("sssNo"));
                        employee.setPagibigNo(rs.getString("pagibigNo"));
                        employee.setPhilhealthNo(rs.getString("philhealthNo"));
                        employee.setTinNo(rs.getString("tinNo"));
                        employee.setFullAddress(rs.getString("fullAddress"));

                        // Now load supervisor name if applicable:
                        if (employee.getSupervisorID() != 0) {
                            // Avoid infinite recursion if supervisorID == employeeID
                            if (employee.getSupervisorID() != employee.getEmployeeID()) {
                                Employee supervisor = getEmployeeByID(employee.getSupervisorID());
                                if (supervisor != null) {
                                    employee.setSupervisorName(supervisor.getLastName() + ", " + supervisor.getFirstName());
                                } else {
                                    employee.setSupervisorName("No Supervisor");
                                }
                            } else {
                                employee.setSupervisorName("Self-Supervised");
                            }
                        } else {
                            employee.setSupervisorName("No Supervisor");
                        }

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