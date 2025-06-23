package ui.base;

import pojo.Employee;
import service.AttendanceService;
import service.EmployeeService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
                getFullNameText().setText(
                    employee.getFirstName() + " " + employee.getLastName()
                );
                getPositionText().setText(employee.getPosition());
            } else {
                getFullNameText().setText("Unknown Employee");
                getPositionText().setText("Unknown Position");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void startClock() {
        clockTimer = new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                LocalDateTime now = LocalDateTime.now();
                // Month day, Year in moderate font
                String datePart = now.format(
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                );
                // Time in 12-hour format with AM/PM
                String timePart = now.format(
                    DateTimeFormatter.ofPattern("hh:mm:ss a")
                );
                // Swing’s HTML font size goes from 1 (smallest) to 7 (largest)
                String html =
                    "<html>" +
                      "<div align='center'>" +
                        "<font size='6'>" + datePart + "</font><br>" +
                        "<font size='10'>" + timePart + "</font>" +
                      "</div>" +
                    "</html>";
                getDateTimeText().setText(html);
            }
        });
        clockTimer.start();
    }

    protected void performClockIn() {
        try {
            if (isWeekend()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot Clock-In during day-off (Saturday or Sunday).");
                return;
            }
            if (isOutsideWorkingHours()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot Clock-In outside 6:50AM–4:00PM.");
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
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Already Clocked-In today.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error during Clock-In: " + e.getMessage());
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
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Clock-Out failed.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error during Clock-Out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void refreshClockInOutStatus() {
        try {
            AttendanceService.AttendanceStatus status =
                attendanceService.getTodayAttendanceStatus(employeeID);

            // --- your existing clock-in/out UI logic ---
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

            // --- monthly total in “X hrs, Y min” ---
            YearMonth ym = YearMonth.now();
            BigDecimal totalDecimal =
                attendanceService.getMonthlyWorkedHours(
                    employeeID, ym.getYear(), ym.getMonthValue()
                );
            BigDecimal totalMinutesBD = totalDecimal.multiply(BigDecimal.valueOf(60));
            long totalMinutes = totalMinutesBD
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
            long hours   = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            String txt = String.format("%d hrs, %d min", hours, minutes);
            getTotalWorkedHoursText().setText(txt);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error refreshing attendance status: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private boolean isWeekend() {
        DayOfWeek d = LocalDate.now().getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private boolean isOutsideWorkingHours() {
        LocalTime now = LocalTime.now();
        return now.isBefore(LocalTime.of(6,50))
            || now.isAfter(LocalTime.of(16,0));
    }

    private String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    // --- subclasses must supply these ---
    protected abstract JLabel  getFullNameText();
    protected abstract JLabel  getPositionText();
    protected abstract JLabel  getDateTimeText();
    protected abstract JLabel  getClockInText();
    protected abstract JLabel  getClockOutText();
    protected abstract JButton getClockInButton();
    protected abstract JButton getClockOutButton();
    protected abstract JLabel  getTotalWorkedHoursText();
}