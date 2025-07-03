package ui.base;

import com.toedter.calendar.JCalendar;
import service.EmployeeService;
import service.UserService;
import util.LightButton;
import util.BlueButton;
import ui.PageHREmployeeRecords;
import db.DatabaseConnection;
import pojo.Employee;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractEmployeeRegisterPage extends JFrame {
  // — UI components — (protected so subclasses can access)
  protected JTextField lastNameField, firstNameField;
  protected JCalendar  dobCal;
  protected JTextField provinceField, cityField, barangayField,
                         streetField, houseNoField, zipField;
  protected JTextField phoneField, sssField, philField, tinField, pagibigField;
  protected JComboBox<String> roleCombo, statusCombo,
                              positionCombo, departmentCombo,
                              supervisorCombo, salaryCombo;
  protected LightButton backButton, cancelButton;
  protected BlueButton  confirmButton;

  // for mapping dropdown selections back to IDs
  protected List<Integer> statusIds, positionIds, departmentIds, supervisorIds;
  private static final String[] ROLE_NAMES = {
    "Employee", "HR", "IT", "Finance", "Manager"
  };

  private final EmployeeService empSvc = new EmployeeService();
  private final UserService     usrSvc = new UserService();
  private final List<JTextField> allFields = new ArrayList<>();
  private boolean dirty = false;

  /**
   * Call once, right after initComponents() in your subclass.
   */
  protected void setupRegisterPage(
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
    // store references
    this.lastNameField   = ln;
    this.firstNameField  = fn;
    this.dobCal          = dc;
    this.provinceField   = prov;
    this.cityField       = city;
    this.barangayField   = brgy;
    this.streetField     = street;
    this.houseNoField    = house;
    this.zipField        = zip;
    this.phoneField      = phone;
    this.sssField        = sss;
    this.philField       = phil;
    this.tinField        = tin;
    this.pagibigField    = pagibig;
    this.roleCombo       = roleC;
    this.statusCombo     = statusC;
    this.positionCombo   = posC;
    this.departmentCombo = deptC;
    this.supervisorCombo = supC;
    this.salaryCombo     = salC;
    this.backButton      = backB;
    this.cancelButton    = cancelB;
    this.confirmButton   = confirmB;

    // ── Populate combos ── //
    roleC.setModel(new DefaultComboBoxModel<>(ROLE_NAMES));
    statusIds = empSvc.getAllStatusIDs();
    statusC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllStatusTypes().toArray(new String[0])
    ));
    positionIds = empSvc.getAllPositionIDs();
    posC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllPositionNames().toArray(new String[0])
    ));
    departmentIds = empSvc.getAllDepartmentIDs();
    deptC.setModel(new DefaultComboBoxModel<>(
      empSvc.getAllDepartmentNames().toArray(new String[0])
    ));
    var emps = empSvc.getAllEmployees();
    supervisorIds = new ArrayList<>();
    var supNames = new Vector<String>();
    for (var e : emps) {
      supervisorIds.add(e.getEmployeeID());
      supNames.add(e.getLastName() + ", " + e.getFirstName());
    }
    supC.setModel(new DefaultComboBoxModel<>(supNames));
    // sync dept when pos changes
    posC.addActionListener(e -> {
      dirty = true; confirmButton.setEnabled(true);
      int idx = posC.getSelectedIndex();
      if (idx < 0) return;
      int deptId = empSvc.getDepartmentIDForPosition(positionIds.get(idx));
      for (int i = 0; i < deptC.getItemCount(); i++) {
        if (departmentIds.get(i) == deptId) {
          deptC.setSelectedIndex(i);
          break;
        }
      }
    });

    // ── Filters & max lengths ── //
    Pattern alpha    = Pattern.compile("[a-zA-Z ]*");
    Pattern alphanum = Pattern.compile("[a-zA-Z0-9 .,#\\-]*");
    installFilter(ln,     alpha,    25);
    installFilter(fn,     alpha,    35);
    installFilter(prov,   alpha,    25);
    installFilter(city,   alpha,    25);
    installFilter(brgy,   alphanum, 25);
    installFilter(street, alphanum, 25);
    installFilter(house,  alphanum, 25);
    installFilter(zip,    Pattern.compile("\\d{0,4}"), 4);
    installFilter(phone,  Pattern.compile("[0-9\\-]*"), 11);
    installFilter(sss,    Pattern.compile("[0-9\\-]*"), 12);
    installFilter(phil,   Pattern.compile("\\d*"),      12);
    installFilter(tin,    Pattern.compile("[0-9\\-]*"), 15);
    installFilter(pagibig,Pattern.compile("\\d*"),      12);

    // ── Min‐length highlights ── //
    installMinValidator(ln,    2);
    installMinValidator(fn,    2);
    installMinValidator(prov,  4);
    installMinValidator(city,  4);
    installMinValidator(brgy,  4);
    installMinValidator(street,4);
    installMinValidator(zip,   4);

    // ── Digit‐count highlights ── //
    installDigitHighlighter(sss, 10);
    installDigitHighlighter(tin,  9);
    installPhoneHighlighter(phone);

    // ── Auto‐formatters ── //
    installFormatter(sss,   this::formatSSS);
    installFormatter(tin,   this::formatTIN);
    installFormatter(phone, this::formatPhone);

    // ── Dirty tracking ── //
    allFields.addAll(List.of(
      ln, fn, prov, city, brgy, street, house, zip,
      phone, sss, phil, tin, pagibig
    ));
    DocumentListener docListener = new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ dirty = true; confirmButton.setEnabled(true); }
      public void removeUpdate(DocumentEvent e){ dirty = true; confirmButton.setEnabled(true); }
      public void changedUpdate(DocumentEvent e){ dirty = true; confirmButton.setEnabled(true); }
    };
    allFields.forEach(f ->
      ((AbstractDocument)f.getDocument()).addDocumentListener(docListener)
    );
    ActionListener markDirty = ev -> {
      dirty = true;
      confirmButton.setEnabled(true);
    };
    for (var c : List.of(roleC, statusC, posC, deptC, supC, salC))
      c.addActionListener(markDirty);
    dc.addPropertyChangeListener("calendar", e -> { dirty = true; confirmButton.setEnabled(true); });

    // ── Back / Cancel ── //
    ActionListener goBack = ev -> {
      if (dirty
       && JOptionPane.showConfirmDialog(
            this,
            "You have unsaved changes. Discard?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
          ) != JOptionPane.YES_NO_OPTION) return;
      new PageHREmployeeRecords().setVisible(true);
      dispose();
    };
    backButton .addActionListener(goBack);
    cancelButton.addActionListener(goBack);

    // ── Confirm ── //
    // start disabled
    confirmButton.setEnabled(false);
    confirmButton.addActionListener(ev -> {
      List<String> errors = validateAll();
      if (!errors.isEmpty()) {
        JOptionPane.showMessageDialog(
          this,
          String.join("\n", errors),
          "Please correct the following:",
          JOptionPane.WARNING_MESSAGE
        );
        return;
      }
      if (JOptionPane.showConfirmDialog(
           this,
           "Are you sure you want to create this new employee?",
           "Confirm",
           JOptionPane.YES_NO_OPTION
         ) != JOptionPane.YES_OPTION) return;
      doRegister();
    });
  }

  private void doRegister() {
    try (Connection c = DatabaseConnection.getInstance().getConnection()) {
      c.setAutoCommit(false);
      // … your existing create‐employee logic …
      c.commit();
      JOptionPane.showMessageDialog(this, "Employee created successfully!");
      new PageHREmployeeRecords().setVisible(true);
      dispose();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(
        this,
        "Failed to create employee: " + ex.getMessage(),
        "Error",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  /** Gathers all validation errors in plain language. */
  protected List<String> validateAll() {
    var errs = new ArrayList<String>();
    if (lastNameField.getText().trim().length()<2)
      errs.add("Please enter a last name (at least 2 letters).");
    if (firstNameField.getText().trim().length()<2)
      errs.add("Please enter a first name (at least 2 letters).");
    if (provinceField.getText().trim().length()<4)
      errs.add("Please enter a province (at least 4 letters).");
    if (cityField.getText().trim().length()<4)
      errs.add("Please enter a city/municipality (at least 4 letters).");
    if (barangayField.getText().trim().length()<4)
      errs.add("Please enter a barangay (at least 4 characters).");
    if (streetField.getText().trim().length()<4)
      errs.add("Please enter a street (at least 4 characters).");
    if (houseNoField.getText().trim().isEmpty())
      errs.add("Please enter a house number.");
    if (zipField.getText().trim().length()!=4)
      errs.add("Please enter a 4-digit ZIP code.");
    if (!phoneField.getText().matches("\\d{3}-\\d{3}-\\d{3}"))
      errs.add("Please enter a phone number in format XXX-XXX-XXX.");
    if (!sssField.getText().matches("\\d{2}-\\d{7}-\\d"))
      errs.add("Please enter an SSS number in format XX-XXXXXXX-X.");
    if (philField.getText().replaceAll("\\D","").length()!=12)
      errs.add("Please enter a 12-digit PhilHealth number.");
    if (pagibigField.getText().replaceAll("\\D","").length()!=12)
      errs.add("Please enter a 12-digit Pag-IBIG number.");
    if (!tinField.getText().matches("\\d{3}-\\d{3}-\\d{3}-000"))
      errs.add("Please enter a TIN in format XXX-XXX-XXX-000.");
    if (roleCombo.getSelectedIndex()<0)
      errs.add("Please select an employee role.");
    if (statusCombo.getSelectedIndex()<0)
      errs.add("Please select an employment status.");
    if (positionCombo.getSelectedIndex()<0)
      errs.add("Please select a position.");
    if (departmentCombo.getSelectedIndex()<0)
      errs.add("Please select a department.");
    if (supervisorCombo.getSelectedIndex()<0)
      errs.add("Please select a supervisor.");
    if (salaryCombo.getSelectedIndex()<0)
      errs.add("Please select a salary.");
    var cal = dobCal.getCalendar();
    LocalDate dob = LocalDate.of(
      cal.get(Calendar.YEAR),
      cal.get(Calendar.MONTH) + 1,
      cal.get(Calendar.DAY_OF_MONTH)
    );
    if (Period.between(dob, LocalDate.now()).getYears() < 18)
      errs.add("Employee must be at least 18 years old.");
    return errs;
  }

  // ───────── Helpers ───────── //
  private void installFilter(JTextField fld, Pattern p, int maxLen) {
    ((AbstractDocument)fld.getDocument())
      .setDocumentFilter(new PatternFilter(p, maxLen));
  }
  private void installMinValidator(JTextField fld, int minLen) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        fld.setBackground(
          fld.getText().trim().length() < minLen
            ? Color.PINK : Color.WHITE
        );
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private void installDigitHighlighter(JTextField fld, int req) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        int c = fld.getText().replaceAll("\\D","").length();
        fld.setBackground(c < req ? Color.PINK : Color.WHITE);
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private void installPhoneHighlighter(JTextField fld) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      private void upd(){
        fld.setBackground(
          fld.getText().matches("\\d{3}-\\d{3}-\\d{3}")
            ? Color.WHITE : Color.PINK
        );
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private static class PatternFilter extends DocumentFilter {
    private final Pattern pat; private final int maxLen;
    PatternFilter(Pattern p,int m){ pat=p; maxLen=m; }
    @Override public void insertString(FilterBypass fb,int offs,String str,AttributeSet a)
      throws BadLocationException {
      String orig = fb.getDocument().getText(0,fb.getDocument().getLength());
      String cand = orig.substring(0,offs) + str + orig.substring(offs);
      if (cand.length()<=maxLen && pat.matcher(cand).matches())
        super.insertString(fb,offs,str,a);
    }
    @Override public void replace(FilterBypass fb,int offs,int len,String str,AttributeSet a)
      throws BadLocationException {
      String orig = fb.getDocument().getText(0,fb.getDocument().getLength());
      String cand = orig.substring(0,offs) + str + orig.substring(offs+len);
      if (cand.length()<=maxLen && pat.matcher(cand).matches())
        super.replace(fb,offs,len,str,a);
    }
  }
  private void installFormatter(JTextField fld, java.util.function.Function<String,String> fmt) {
    fld.getDocument().addDocumentListener(new DocumentListener(){
      boolean busy=false;
      private void upd(){
        if (busy) return;
        String digits = fld.getText().replaceAll("\\D","");
        String out    = fmt.apply(digits);
        if (!fld.getText().equals(out)) {
          busy = true;
          SwingUtilities.invokeLater(() -> {
            fld.setText(out);
            busy = false;
          });
        }
      }
      public void insertUpdate(DocumentEvent e){upd();}
      public void removeUpdate(DocumentEvent e){upd();}
      public void changedUpdate(DocumentEvent e){upd();}
    });
  }
  private String formatSSS(String d) {
    if (d.length()>10) d=d.substring(0,10);
    if (d.length()<3) return d;
    if (d.length()<=9) return d.substring(0,2)+"-"+d.substring(2);
    return d.substring(0,2)+"-"+d.substring(2,9)+"-"+d.substring(9);
  }
  private String formatTIN(String d) {
    if (d.length()>9) d=d.substring(0,9);
    if (d.length()<3) return d;
    var sb = new StringBuilder();
    if (d.length()<=3) sb.append(d);
    else if (d.length()<=6) sb.append(d,0,3).append("-").append(d.substring(3));
    else sb.append(d,0,3).append("-").append(d,3,6).append("-").append(d.substring(6));
    if (d.length()==9) sb.append("-000");
    return sb.toString();
  }
  private String formatPhone(String d) {
    if (d.length()>9) d=d.substring(d.length()-9);
    if (d.length()<=3) return d;
    if (d.length()<=6) return d.substring(0,3)+"-"+d.substring(3);
    return d.substring(0,3)+"-"+d.substring(3,6)+"-"+d.substring(6);
  }
  private String capitalize(String s) {
    if (s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0))+s.substring(1).toLowerCase();
  }
  private String makeEmail(String fn, String ln) {
    return (fn.charAt(0)+ln).toLowerCase()+"@motor.ph";
  }
  private BigDecimal parseMoney(String s) {
    return new BigDecimal(s.replaceAll("[₱, ]",""));
  }
}