package service;

import dao.AttendanceDAO;
import daoimpl.AttendanceDAOImpl;
import pojo.Attendance;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class AttendanceService {

    private AttendanceDAO attendanceDAO;

    public AttendanceService() {
        try {
            attendanceDAO = new AttendanceDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing AttendanceDAO", e);
        }
    }

    public Attendance getAttendanceByID(int attendanceID) {
        try {
            return attendanceDAO.getAttendanceByID(attendanceID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by ID", e);
        }
    }

    public List<Attendance> getAttendanceByEmployeeID(int employeeID) {
        try {
            return attendanceDAO.getAttendanceByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by employeeID", e);
        }
    }

    public List<Attendance> getAttendanceByDate(Date date) {
        try {
            return attendanceDAO.getAttendanceByDate(date);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving attendance by date", e);
        }
    }

    public List<Attendance> getAllAttendance() {
        try {
            return attendanceDAO.getAllAttendance();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all attendance records", e);
        }
    }

    public void addAttendance(Attendance attendance) {
        try {
            attendanceDAO.addAttendance(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding attendance record", e);
        }
    }

    public void updateAttendance(Attendance attendance) {
        try {
            attendanceDAO.updateAttendance(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating attendance record", e);
        }
    }

    public void deleteAttendance(int attendanceID) {
        try {
            attendanceDAO.deleteAttendance(attendanceID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting attendance record", e);
        }
    }
}