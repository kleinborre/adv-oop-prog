package pojo;

import java.sql.Date;
import java.sql.Time;

public class SystemManagement {

    private int employeeID;
    private String employeeName;
    private Date date;
    private Time loginTime;
    private Time logoutTime;
    private double workedHours;

    // Constructors

    public SystemManagement() {
    }

    public SystemManagement(int employeeID, String employeeName, Date date, Time loginTime, Time logoutTime, double workedHours) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.date = date;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.workedHours = workedHours;
    }

    // Getters and Setters

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Time loginTime) {
        this.loginTime = loginTime;
    }

    public Time getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Time logoutTime) {
        this.logoutTime = logoutTime;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
    }
}