package service;

import dao.EmployeeDAO;
import daoimpl.EmployeeDAOImpl;
import pojo.Employee;

import java.sql.SQLException;
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
            return employeeDAO.getEmployeeByID(employeeID);
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