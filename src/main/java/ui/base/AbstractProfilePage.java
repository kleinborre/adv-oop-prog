package ui.base;

import javax.swing.JLabel;
import service.EmployeeService;
import pojo.Employee;
import util.SessionManager;

public abstract class AbstractProfilePage extends javax.swing.JFrame {

    protected EmployeeService employeeService;

    public AbstractProfilePage() {
        employeeService = new EmployeeService();
    }

    protected void initializeProfilePage() {
        try {
            int employeeID = SessionManager.getEmployeeID();

            Employee employee = employeeService.getEmployeeByID(employeeID);

            if (employee == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "Employee record not found.");
                return;
            }

            // Map fields
            getEmployeeIDText().setText(String.valueOf(employee.getEmployeeID()));
            getPositionText().setText(employee.getPosition());
            getFirstNameText().setText(employee.getFirstName());
            getLastNameText().setText(employee.getLastName());
            getBirthdayText().setText(employee.getBirthDate().toString());
            getPhoneNumberText().setText(employee.getPhoneNo());
            getAddressText().setText("<html>" + employee.getFullAddress().trim().replaceAll(",\\s*", "<br>") + "</html>");
            getSupervisorText().setText(employee.getSupervisorName());
            getStatusText().setText(employee.getStatusDesc());
            getSSSNumberText().setText(employee.getSssNo());
            getPagibigNumberText().setText(employee.getPagibigNo());
            getPhilhealthNumberText().setText(employee.getPhilhealthNo());
            getTINNumberText().setText(employee.getTinNo());

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Abstract methods to be implemented by the concrete page

    protected abstract JLabel getEmployeeIDText();
    protected abstract JLabel getPositionText();
    protected abstract JLabel getFirstNameText();
    protected abstract JLabel getLastNameText();
    protected abstract JLabel getBirthdayText();
    protected abstract JLabel getPhoneNumberText();
    protected abstract JLabel getAddressText();
    protected abstract JLabel getSupervisorText();
    protected abstract JLabel getStatusText();
    protected abstract JLabel getSSSNumberText();
    protected abstract JLabel getPagibigNumberText();
    protected abstract JLabel getPhilhealthNumberText();
    protected abstract JLabel getTINNumberText();
}