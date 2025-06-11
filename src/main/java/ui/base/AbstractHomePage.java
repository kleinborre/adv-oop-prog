package ui.base;

import pojo.Employee;
import service.AttendanceService;
import service.EmployeeService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;

public abstract class AbstractHomePage extends JFrame {

    protected String userID;
    protected int employeeID;

    protected Timer clockTimer;

    protected boolean isClockedInToday = false;
    protected boolean isClockedOutToday = false;

    protected AttendanceService attendanceService = new AttendanceService();

    protected void initializeHomePage(String userID, int employeeID) {
        this.userID = userID;
        this.employeeID = employeeID;
        loadEmployeeInfo();
        startClock();
        refreshClockInOutStatus();
    }

    protected void loadEmployeeInfo() {
        try {
            EmployeeService employeeService = new EmployeeService();
            Employee employee = employeeService.getEmployeeByID(employeeID);

            if (employee != null) {
                getFullNameText().setText(employee.getFirstName() + " " + employee.getLastName());
                getPositionText().setText(employee.getPosition());
            } else {
                getFullNameText().setText("Unknown Employee");
                getPositionText().setText("Unknown Position");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employee info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void startClock() {
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                getDateTimeText().setText(now.format(formatter));
            }
        });
        clockTimer.start();
    }

    protected void performClockIn() {
        try {
            if (isWeekend()) {
                JOptionPane.showMessageDialog(this, "Cannot Clock-In during day-off (Saturday or Sunday).");
                return;
            }

            if (isOutsideWorkingHours()) {
                JOptionPane.showMessageDialog(this, "Cannot Clock-In beyond working hours (allowed: 6:50AM to 4:00PM).");
                return;
            }

            if (isClockedInToday) {
                JOptionPane.showMessageDialog(this, "Already Clocked-In today.");
                return;
            }

            boolean success = attendanceService.clockIn(employeeID);

            if (success) {
                isClockedInToday = true;
                getClockInText().setText(getCurrentTime());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(true);
                JOptionPane.showMessageDialog(this, "Clock-In successful!");
            } else {
                JOptionPane.showMessageDialog(this, "Already Clocked-In today.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during Clock-In: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void performClockOut() {
        try {
            if (!isClockedInToday) {
                JOptionPane.showMessageDialog(this, "You must Clock-In first.");
                return;
            }

            if (isClockedOutToday) {
                JOptionPane.showMessageDialog(this, "Already Clocked-Out today.");
                return;
            }

            boolean success = attendanceService.clockOut(employeeID);

            if (success) {
                isClockedOutToday = true;
                getClockOutText().setText(getCurrentTime());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(false);
                JOptionPane.showMessageDialog(this, "Clock-Out successful!");
            } else {
                JOptionPane.showMessageDialog(this, "Clock-Out failed.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during Clock-Out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void refreshClockInOutStatus() {
        try {
            AttendanceService.AttendanceStatus status = attendanceService.getTodayAttendanceStatus(employeeID);

            if (status.isClockedIn()) {
                isClockedInToday = true;
                getClockInText().setText(status.getLogIn());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(!status.isClockedOut());
            } else {
                isClockedInToday = false;
                getClockInText().setText("Not Clocked-In");
                getClockInButton().setEnabled(true);
                getClockOutButton().setEnabled(false);
            }

            if (status.isClockedOut()) {
                isClockedOutToday = true;
                getClockOutText().setText(status.getLogOut());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(false);
            } else {
                isClockedOutToday = false;
                getClockOutText().setText("Not Clocked-Out");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error refreshing attendance status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isWeekend() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isOutsideWorkingHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(6, 50);
        LocalTime end = LocalTime.of(16, 0);
        return now.isBefore(start) || now.isAfter(end);
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    // Abstract getters that must be implemented in concrete page:

    protected abstract JLabel getFullNameText();
    protected abstract JLabel getPositionText();
    protected abstract JLabel getDateTimeText();
    protected abstract JLabel getClockInText();
    protected abstract JLabel getClockOutText();
    protected abstract JButton getClockInButton();
    protected abstract JButton getClockOutButton();
}