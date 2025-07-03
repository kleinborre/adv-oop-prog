package ui.base;

import com.toedter.calendar.JCalendar;
import service.EmployeeService;
import util.LightButton;
import util.BlueButton;
import util.SessionManager;
import db.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

public abstract class AbstractEmployeeUpdatePage extends AbstractEmployeeRegisterPage {
  protected final EmployeeService empSvc = new EmployeeService();

  // comes from the records double‐click
  protected final int selectedEmployeeID = SessionManager.getSelectedEmployeeID();
  private String userID;  // so we don’t overwrite your login session

  /** Call *after* initComponents() in your subclass */
  protected void setupUpdatePage(
    JTextField ln, JTextField fn, JCalendar dc,
    JTextField prov, JTextField city, JTextField brgy,
    JTextField street, JTextField house, JTextField zip,
    JTextField phone, JTextField sss, JTextField phil,
    JTextField tin, JTextField pagibig,
    JComboBox<String> roleC, JComboBox<String> statusC,
    JComboBox<String> posC, JComboBox<String> deptC,
    JComboBox<String> supC, JComboBox<String> salC,
    LightButton backB, LightButton cancelB, BlueButton confirmB
  ) {
    // 1) first do all the normal register-page wiring:
    super.setupRegisterPage(
      ln, fn, dc,
      prov, city, brgy,
      street, house, zip,
      phone, sss, phil,
      tin, pagibig,
      roleC, statusC,
      posC, deptC,
      supC, salC,
      backB, cancelB, confirmB
    );

    // — Strip off the “create new” listener so it never fires in update mode —
    for (ActionListener al : confirmB.getActionListeners()) {
      confirmB.removeActionListener(al);
    }

    // 2) salary is read-only
    salC.setEnabled(false);

    // 3) relabel the button
    confirmB.setText("Update");

    // 4) load existing employee data:
    var e = empSvc.getEmployeeByID(selectedEmployeeID);
    this.userID = e.getUserID();

    ln.setText(     e.getLastName());
    fn.setText(     e.getFirstName());
    dc.getCalendar().setTime(e.getBirthDate());
    prov.setText(   e.getProvince());
    city.setText(   e.getCity());
    brgy.setText(   e.getBarangay());
    street.setText(e.getStreet());
    house.setText( e.getHouseNo());
    zip.setText(   e.getZipCode()==null? "" : e.getZipCode().toString());
    phone.setText(e.getPhoneNo());
    sss.setText(  e.getSssNo());
    phil.setText( e.getPhilhealthNo());
    tin.setText(  e.getTinNo());
    pagibig.setText(e.getPagibigNo());

    roleC .setSelectedIndex(e.getUserID().charAt(1) - '1');
    statusC.setSelectedIndex(e.getStatusID()    - 1);
    posC   .setSelectedIndex(e.getPositionID()  - 1);
    deptC  .setSelectedIndex(e.getDepartmentID()- 1);
    supC   .setSelectedItem(e.getSupervisorName());

    // 5) disable “Update” until any change occurs
    confirmB.setEnabled(false);
    DocumentListener dl = new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ confirmB.setEnabled(true); }
      public void removeUpdate(DocumentEvent e){ confirmB.setEnabled(true); }
      public void changedUpdate(DocumentEvent e){ confirmB.setEnabled(true); }
    };
    for (JTextField fld : List.of(
      ln,fn,prov,city,brgy,street,house,zip,
      phone,sss,phil,tin,pagibig
    )) {
      fld.getDocument().addDocumentListener(dl);
    }
    ActionListener comboListener = ev -> confirmB.setEnabled(true);
    for (var cb : List.of(roleC, statusC, posC, deptC, supC)) {
      cb.addActionListener(comboListener);
    }
    dc.addPropertyChangeListener("calendar", ev -> confirmB.setEnabled(true));

    // 6) now attach *only* the UPDATE logic:
    confirmB.addActionListener(ev -> {
      var errs = validateAll();
      if (!errs.isEmpty()) {
        JOptionPane.showMessageDialog(
          this,
          String.join("\n", errs),
          "Please correct the following:",
          JOptionPane.WARNING_MESSAGE
        );
        return;
      }
      if (JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to update this employee?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
          ) != JOptionPane.YES_OPTION) {
        return;
      }
      doUpdate();
    });
  }

  private void doUpdate() {
    try (Connection c = DatabaseConnection
                         .getInstance()
                         .getConnection()) {
      c.setAutoCommit(false);

      // 1) authentication → update only roleID
      try (PreparedStatement p = c.prepareStatement(
             "UPDATE authentication SET roleID=? WHERE userID=?"
           )) {
        p.setInt   (1, roleCombo.getSelectedIndex() + 1);
        p.setString(2, userID);
        p.executeUpdate();
      }

      // 2) employee → regenerate email so chk_emp_email_format passes
      try (PreparedStatement p = c.prepareStatement(
             "UPDATE employee SET "
           + "firstName=?, lastName=?, birthDate=?, phoneNo=?, email=?, "
           + "statusID=?, positionID=?, departmentID=?, supervisorID=? "
           + "WHERE employeeID=?"
           )) {
        String fn = firstNameField.getText().trim();
        String ln = lastNameField .getText().trim();
        p.setString(1, fn);
        p.setString(2, ln);
        p.setDate  (3, new java.sql.Date(
                       dobCal.getCalendar().getTimeInMillis()
                     ));
        p.setString(4, phoneField.getText().trim());
        p.setString(5, makeEmail(fn, ln));
        p.setInt   (6, statusIds.get(statusCombo.getSelectedIndex()));
        p.setInt   (7, positionIds.get(positionCombo.getSelectedIndex()));
        p.setInt   (8, departmentIds.get(departmentCombo.getSelectedIndex()));
        p.setInt   (9, supervisorIds.get(supervisorCombo.getSelectedIndex()));
        p.setInt   (10, selectedEmployeeID);
        p.executeUpdate();
      }

      // 3) address → allow zip NULL
      try (PreparedStatement p = c.prepareStatement(
             "UPDATE address a "
           + "JOIN employeeaddress ea ON a.addressID=ea.addressID "
           + "  AND ea.employeeID=? "
           + "SET houseNo=?, street=?, barangay=?, city=?, province=?, zipCode=?"
           )) {
        p.setInt(1, selectedEmployeeID);
        p.setString(2, houseNoField.getText().trim());
        p.setString(3, streetField .getText().trim());
        p.setString(4, barangayField.getText().trim());
        p.setString(5, cityField   .getText().trim());
        p.setString(6, provinceField.getText().trim());
        String rawZip = zipField.getText().trim();
        if (rawZip.isEmpty()) {
          p.setNull(7, Types.INTEGER);
        } else {
          p.setInt(7, Integer.parseInt(rawZip));
        }
        p.executeUpdate();
      }

      // 4) govid
      try (PreparedStatement p = c.prepareStatement(
             "UPDATE govid SET sss=?, philhealth=?, tin=?, pagibig=? "
           + "WHERE employeeID=?"
           )) {
        p.setString(1, sssField.getText().trim());
        p.setString(2, philField.getText().trim());
        p.setString(3, tinField.getText().trim());
        p.setString(4, pagibigField.getText().trim());
        p.setInt   (5, selectedEmployeeID);
        p.executeUpdate();
      }

      c.commit();
      JOptionPane.showMessageDialog(
        this,
        "Employee updated successfully!",
        "Success",
        JOptionPane.INFORMATION_MESSAGE
      );
      new ui.PageHREmployeeRecords().setVisible(true);
      dispose();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
        this,
        "Failed to update employee: " + ex.getMessage(),
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  /** Rebuilds the email to satisfy the CHECK constraint */
  private String makeEmail(String fn, String ln) {
    String firstInitial = fn.isEmpty() ? "" : fn.substring(0,1).toLowerCase();
    String lastNoSpace  = ln.replaceAll("\\s+","").toLowerCase();
    return firstInitial + lastNoSpace + "@motor.ph";
  }
}