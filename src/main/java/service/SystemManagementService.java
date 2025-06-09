package service;

import dao.SystemManagementDAO;
import daoimpl.SystemManagementDAOImpl;
import pojo.SystemManagement;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class SystemManagementService {

    private SystemManagementDAO systemManagementDAO;

    public SystemManagementService() {
        try {
            systemManagementDAO = new SystemManagementDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing SystemManagementDAO", e);
        }
    }

    public List<SystemManagement> getAllSystemLogs() {
        try {
            return systemManagementDAO.getAllSystemLogs();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all system logs", e);
        }
    }

    public List<SystemManagement> getSystemLogsByEmployeeID(int employeeID) {
        try {
            return systemManagementDAO.getSystemLogsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving system logs by employee ID", e);
        }
    }

    public List<SystemManagement> getSystemLogsByDate(Date date) {
        try {
            return systemManagementDAO.getSystemLogsByDate(date);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving system logs by date", e);
        }
    }
}