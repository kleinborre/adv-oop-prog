package ui.base;

import pojo.Employee;
import service.AttendanceService;
import service.EmployeeService;
import util.SessionManager;
import ui.PageLogin;

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

    private Timer clockTimer;
    private LocalDate lastAutoDate = null;

    public boolean isClockedInToday  = false;
    public boolean isClockedOutToday = false;

    protected AttendanceService attendanceService = new AttendanceService();

    protected void initializeHomePage(String userID, int employeeID) {
        this.userID     = userID;
        this.employeeID = employeeID;
        loadEmployeeInfo();
        startClock();
        refreshClockInOutStatus();
    }

    private void loadEmployeeInfo() {
        try {
            Employee e = new EmployeeService().getEmployeeByID(employeeID);
            if (e != null) {
                getFullNameText().setText(e.getFirstName() + " " + e.getLastName());
                getPositionText().setText(e.getPosition());
            } else {
                getFullNameText().setText("Unknown Employee");
                getPositionText().setText("Unknown Position");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee info: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    protected void startClock() {
        clockTimer = new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent evt) {
                LocalDateTime now = LocalDateTime.now();

                // date + big time
                String datePart = now.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                String timePart = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                String html =
                  "<html><div align='center'>"
                +   "<font size='6'>" + datePart + "</font><br>"
                +   "<font size='8'>" + timePart + "</font>"
                + "</div></html>";
                getDateTimeText().setText(html);

                // auto–clock-out at 6:50am once/day
                if (now.getHour()==6 && now.getMinute()==50) {
                    LocalDate today = now.toLocalDate();
                    if (!today.equals(lastAutoDate)) {
                        try {
                            autoClockOutYesterday();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        lastAutoDate = today;
                        refreshClockInOutStatus();
                    }
                }
            }
        });
        clockTimer.start();
    }

    private void autoClockOutYesterday() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime cutoff    = LocalTime.of(6,50);
        attendanceService.autoClockOutForDate(employeeID, yesterday, cutoff);
    }

    public void performClockIn() {
        try {
            if (isWeekend()) {
                JOptionPane.showMessageDialog(this,
                  "Cannot Clock-In during day-off (Saturday or Sunday).");
                return;
            }
            if (isOutsideWorkingHours()) {
                JOptionPane.showMessageDialog(this,
                  "Cannot Clock-In outside 6:50 AM–4:00 PM.");
                return;
            }
            if (isClockedInToday) {
                JOptionPane.showMessageDialog(this, "Already Clocked-In today.");
                return;
            }

            boolean ok = attendanceService.clockIn(employeeID);
            if (ok) {
                isClockedInToday = true;
                getClockInText().setText(getCurrentTime());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(true);
                JOptionPane.showMessageDialog(this, "Clock-In successful!");
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Already Clocked-In today.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error during Clock-In: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void performClockOut() {
        try {
            if (!isClockedInToday) {
                JOptionPane.showMessageDialog(this, "You must Clock-In first.");
                return;
            }
            if (isClockedOutToday) {
                JOptionPane.showMessageDialog(this, "Already Clocked-Out today.");
                return;
            }

            boolean ok = attendanceService.clockOut(employeeID);
            if (ok) {
                isClockedOutToday = true;
                getClockOutText().setText(getCurrentTime());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(false);
                JOptionPane.showMessageDialog(this, "Clock-Out successful!");
                refreshClockInOutStatus();
            } else {
                JOptionPane.showMessageDialog(this, "Clock-Out failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error during Clock-Out: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void refreshClockInOutStatus() {
        try {
            AttendanceService.AttendanceStatus s =
              attendanceService.getTodayAttendanceStatus(employeeID);

            // in/out UI logic
            if (s.isClockedIn()) {
                isClockedInToday = true;
                getClockInText().setText(s.getLogIn());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(!s.isClockedOut());
            } else {
                isClockedInToday = false;
                getClockInText().setText("Not Clocked-In");
                getClockInButton().setEnabled(true);
                getClockOutButton().setEnabled(false);
            }
            if (s.isClockedOut()) {
                isClockedOutToday = true;
                getClockOutText().setText(s.getLogOut());
                getClockInButton().setEnabled(false);
                getClockOutButton().setEnabled(false);
            } else {
                isClockedOutToday = false;
                getClockOutText().setText("Not Clocked-Out");
            }

            // monthly total “X hrs, Y min”
            YearMonth ym = YearMonth.now();
            BigDecimal decHrs = attendanceService.getMonthlyWorkedHours(
                employeeID, ym.getYear(), ym.getMonthValue()
            );
            BigDecimal totalMin = decHrs.multiply(BigDecimal.valueOf(60));
            long mins = totalMin.setScale(0, RoundingMode.HALF_UP).longValue();
            long hrs  = mins / 60;
            long rem  = mins % 60;
            getTotalWorkedHoursText().setText(String.format("%d hrs, %d min", hrs, rem));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
              "Error refreshing attendance status: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** Wire the logout button with a confirm–dialog. */
    protected void initLogoutButton(JButton logoutButton) {
        logoutButton.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (ans == JOptionPane.YES_OPTION) {
                SessionManager.clearSession();
                new PageLogin().setVisible(true);
                dispose();
            }
            // NO or CLOSED → do nothing
        });
    }

    private boolean isWeekend() {
        DayOfWeek d = LocalDate.now().getDayOfWeek();
        return d==DayOfWeek.SATURDAY || d==DayOfWeek.SUNDAY;
    }
    private boolean isOutsideWorkingHours() {
        LocalTime t = LocalTime.now();
        return t.isBefore(LocalTime.of(6,50)) || t.isAfter(LocalTime.of(16,0));
    }
    private String getCurrentTime() {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // --- Subclasses supply these getters:
    protected abstract JLabel  getFullNameText();
    protected abstract JLabel  getPositionText();
    protected abstract JLabel  getDateTimeText();
    protected abstract JLabel  getClockInText();
    protected abstract JLabel  getClockOutText();
    protected abstract JButton getClockInButton();
    protected abstract JButton getClockOutButton();
    protected abstract JLabel  getTotalWorkedHoursText();
}