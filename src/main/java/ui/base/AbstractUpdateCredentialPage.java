package ui.base;

import javax.swing.*;
import java.awt.Color;
import pojo.User;
import pojo.Employee;
import service.UserService;
import service.EmployeeService;
import util.SessionManager;

public abstract class AbstractUpdateCredentialPage extends JFrame {

    protected UserService userService;
    protected EmployeeService employeeService;
    protected User currentUser;
    protected Employee currentEmployee;

    // injected by subclass via setter
    protected JPasswordField passwordCurrentField;
    protected JPasswordField passwordNewField;
    protected JPasswordField passwordReEnterField;
    protected JLabel errorMessageLabel;

    protected boolean isDirty = false;

    public AbstractUpdateCredentialPage() {
        this.userService = new UserService();
        this.employeeService = new EmployeeService();

        // load user from session
        String userID = SessionManager.getUserID();
        if (userID == null || userID.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Error: No user session found.",
                "Session Error",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        // fetch current user & employee
        currentUser = userService.getUserByUserID(userID);
        int empID = SessionManager.getEmployeeID();
        currentEmployee = employeeService.getEmployeeByID(empID);

        if (currentUser == null || currentEmployee == null) {
            JOptionPane.showMessageDialog(
                this,
                "Error: Could not load your account data.",
                "Data Error",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }
    }

    /**
     * Wire all the top‐of‐page labels (username, email, employeeID, etc.)
     * Must be called by subclass after initComponents() and setPasswordFieldsAndErrorLabel(...)
     */
    protected void initializeUpdateCredentialPage() {
        // top section
        getUsernameText().setText(currentUser.getUsername());
        getEmailText().setText(currentUser.getEmail());

        // side‐bar employee info
        getEmployeeIDText().setText(String.valueOf(currentEmployee.getEmployeeID()));
        getPositionText().setText(currentEmployee.getPosition());
        getSupervisorText().setText(currentEmployee.getSupervisorName());
    }

    /**
     * Must be called by subclass right after initComponents()
     */
    protected void setPasswordFieldsAndErrorLabel(
        JPasswordField current,
        JPasswordField next,
        JPasswordField reenter,
        JLabel errorLabel
    ) {
        this.passwordCurrentField = current;
        this.passwordNewField     = next;
        this.passwordReEnterField = reenter;
        this.errorMessageLabel    = errorLabel;
    }

    // --- password‐update workflow (unchanged) ---

    protected boolean validateCurrentPassword() {
        String input = new String(passwordCurrentField.getPassword());
        return input.equals(currentUser.getPassword());
    }

    protected boolean validateNewPasswordStrength(String pw) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return pw.matches(regex);
    }

    protected boolean doPasswordsMatch() {
        String np = new String(passwordNewField.getPassword());
        String rp = new String(passwordReEnterField.getPassword());
        return np.equals(rp);
    }

    protected void showError(String msg) {
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setText(msg);
    }

    protected void clearError() {
        errorMessageLabel.setText("");
    }

    protected void markDirty() {
        isDirty = true;
    }

    protected void handleCancelOrBack(Runnable nav) {
        if (isDirty) {
            int res = JOptionPane.showConfirmDialog(
                this,
                "Discard your changes?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
            );
            if (res == JOptionPane.YES_OPTION) {
                nav.run();
            }
        } else {
            nav.run();
        }
    }

    protected boolean validateFieldsAndShowErrors() {
        clearError();
        resetFieldBackground(passwordCurrentField);
        resetFieldBackground(passwordNewField);
        resetFieldBackground(passwordReEnterField);

        String cur = new String(passwordCurrentField.getPassword());
        String nw  = new String(passwordNewField.getPassword());
        String re  = new String(passwordReEnterField.getPassword());

        if (cur.isEmpty()) {
            showError("Current password is required.");
            highlightField(passwordCurrentField);
            return false;
        }
        if (!validateCurrentPassword()) {
            showError("Current password is incorrect.");
            highlightField(passwordCurrentField);
            return false;
        }
        if (nw.isEmpty()) {
            showError("New password is required.");
            highlightField(passwordNewField);
            return false;
        }
        if (!validateNewPasswordStrength(nw)) {
            showError("New password too weak.");
            highlightField(passwordNewField);
            return false;
        }
        if (re.isEmpty()) {
            showError("Please re‐enter new password.");
            highlightField(passwordReEnterField);
            return false;
        }
        if (!nw.equals(re)) {
            showError("Passwords do not match.");
            highlightField(passwordNewField);
            highlightField(passwordReEnterField);
            return false;
        }
        return true;
    }

    protected void handleUpdate() {
        if (!validateFieldsAndShowErrors()) return;

        int res = JOptionPane.showConfirmDialog(
            this,
            "Really update your password?",
            "Confirm Update",
            JOptionPane.YES_NO_OPTION
        );
        if (res == JOptionPane.YES_OPTION) {
            currentUser.setPassword(new String(passwordNewField.getPassword()));
            try {
                userService.updateUser(currentUser);
                JOptionPane.showMessageDialog(this, "Password updated!");
                isDirty = false;
            } catch (Exception ex) {
                showError("Failed to update. Try again.");
            }
        }
    }

    protected void highlightField(JPasswordField f) {
        f.setBackground(new Color(0xFFCCCC));
    }
    protected void resetFieldBackground(JPasswordField f) {
        f.setBackground(Color.WHITE);
    }

    // --- abstract getters for all UI labels the abstract needs to wire ---

    protected abstract JLabel getUsernameText();
    protected abstract JLabel getEmailText();
    protected abstract JLabel getEmployeeIDText();
    protected abstract JLabel getPositionText();
    protected abstract JLabel getSupervisorText();
}
