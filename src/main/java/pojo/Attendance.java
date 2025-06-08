package pojo;

import java.sql.Date;
import java.sql.Time;

public class Attendance {

    private int attendanceID;
    private Date date;
    private Time timeIn;
    private Time timeOut;
    private double workedHours;
    private int employeeID;

    // Constructors

    public Attendance() {
    }

    public Attendance(int attendanceID, Date date, Time timeIn, Time timeOut, double workedHours, int employeeID) {
        this.attendanceID = attendanceID;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.workedHours = workedHours;
        this.employeeID = employeeID;
    }

    // Getters and Setters

    public int getAttendanceID() {
        return attendanceID;
    }

    public void setAttendanceID(int attendanceID) {
        this.attendanceID = attendanceID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Time timeIn) {
        this.timeIn = timeIn;
    }

    public Time getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Time timeOut) {
        this.timeOut = timeOut;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }
}