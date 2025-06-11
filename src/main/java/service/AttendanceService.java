package service;

import dao.AttendanceDAO;
import daoimpl.AttendanceDAOImpl;
import pojo.Attendance;

import java.sql.*;
import java.util.List;

import db.DatabaseConnection;

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

    // ==== NEW METHODS → for Clock In / Clock Out ===== //

    public boolean clockIn(int employeeID) throws SQLException {
        String query = "INSERT INTO attendance (employeeID, date, logIn) VALUES (?, CURRENT_DATE, CURRENT_TIME)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeID);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean clockOut(int employeeID) throws SQLException {
        String query = "UPDATE attendance SET logOut = CURRENT_TIME " +
                       "WHERE employeeID = ? AND date = CURRENT_DATE AND logOut IS NULL";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeID);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Inner class for AttendanceStatus
    public static class AttendanceStatus {
        private boolean clockedIn;
        private boolean clockedOut;
        private String logIn;
        private String logOut;

        public boolean isClockedIn() { return clockedIn; }
        public void setClockedIn(boolean clockedIn) { this.clockedIn = clockedIn; }

        public boolean isClockedOut() { return clockedOut; }
        public void setClockedOut(boolean clockedOut) { this.clockedOut = clockedOut; }

        public String getLogIn() { return logIn; }
        public void setLogIn(String logIn) { this.logIn = logIn; }

        public String getLogOut() { return logOut; }
        public void setLogOut(String logOut) { this.logOut = logOut; }
    }

    public AttendanceStatus getTodayAttendanceStatus(int employeeID) throws SQLException {
        AttendanceStatus status = new AttendanceStatus();

        String query = "SELECT logIn, logOut " +
                       "FROM attendance " +
                       "WHERE employeeID = ? AND date = CURRENT_DATE";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Time logIn = rs.getTime("logIn");
                    Time logOut = rs.getTime("logOut");

                    if (logIn != null) {
                        status.setClockedIn(true);
                        status.setLogIn(logIn.toString());
                    } else {
                        status.setClockedIn(false);
                        status.setLogIn("Not Clocked-In");
                    }

                    if (logOut != null) {
                        status.setClockedOut(true);
                        status.setLogOut(logOut.toString());
                    } else {
                        status.setClockedOut(false);
                        status.setLogOut("Not Clocked-Out");
                    }
                } else {
                    // No record today → default
                    status.setClockedIn(false);
                    status.setClockedOut(false);
                    status.setLogIn("Not Clocked-In");
                    status.setLogOut("Not Clocked-Out");
                }
            }
        }

        return status;
    }
}