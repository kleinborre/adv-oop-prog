package dao;

import pojo.SystemManagement;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface SystemManagementDAO {

    List<SystemManagement> getAllSystemLogs() throws SQLException;

    List<SystemManagement> getSystemLogsByEmployeeID(int employeeID) throws SQLException;

    List<SystemManagement> getSystemLogsByDate(Date date) throws SQLException;
}