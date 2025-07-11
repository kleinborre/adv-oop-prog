package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import ui.PageHRPayroll;

public class HRPayrollPageTest {

    PageHRPayroll page;

    // Helper for robust private field access
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object instance, String fieldName, Class<T> type) {
        try {
            java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException("Could not access field: " + fieldName, e);
        }
    }

    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10006", 10006); // HR login
        SwingUtilities.invokeAndWait(() -> page = new PageHRPayroll());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    @Test
    void testAllComponentsPresentAndEnabled() {
        JButton backButton           = getPrivateField(page, "backButton", JButton.class);
        JButton printPayrollButton   = getPrivateField(page, "printPayrollButton", JButton.class);
        com.toedter.calendar.JDateChooser JDateChooser = getPrivateField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        JTable payrollTable          = getPrivateField(page, "payrollTable", JTable.class);
        JScrollPane jScrollPane1     = getPrivateField(page, "jScrollPane1", JScrollPane.class);
        JTextField totalGrossField   = getPrivateField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = getPrivateField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField    = getPrivateField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField        = getPrivateField(page, "totalNetPayField", JTextField.class);

        assertNotNull(backButton, "Back button exists");
        assertNotNull(printPayrollButton, "Print Payroll button exists");
        assertNotNull(JDateChooser, "JDateChooser exists");
        assertNotNull(payrollTable, "Payroll table exists");
        assertNotNull(jScrollPane1, "Table scroll pane exists");
        assertNotNull(totalGrossField, "Total Gross field exists");
        assertNotNull(totalContributionsField, "Total Contributions field exists");
        assertNotNull(totalDeductionsField, "Total Deductions field exists");
        assertNotNull(totalNetPayField, "Total Net Pay field exists");

        assertTrue(backButton.isEnabled(), "Back button enabled");
        assertTrue(printPayrollButton.isEnabled(), "Print Payroll enabled");
        assertTrue(JDateChooser.isEnabled(), "JDateChooser enabled");
    }

    @Test
    void testTableAndScrollPaneSetup() throws Exception {
        JTable payrollTable         = getPrivateField(page, "payrollTable", JTable.class);
        JScrollPane jScrollPane1    = getPrivateField(page, "jScrollPane1", JScrollPane.class);

        // Set JDateChooser to June 3, 2024 to ensure relevant data is loaded
        com.toedter.calendar.JDateChooser JDateChooser = getPrivateField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        SwingUtilities.invokeAndWait(() -> {
            JDateChooser.setDate(june3);
            // The table and fields should auto-refresh via property change
        });

        // Wait for table to load if needed (max 2s)
        int retries = 0;
        while (payrollTable.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }

        assertSame(payrollTable, jScrollPane1.getViewport().getView(), "Table in scroll pane");

        TableModel model = payrollTable.getModel();
        assertTrue(model.getColumnCount() >= 8, "Table has correct columns");
        assertTrue(model.getRowCount() > 0, "Table has at least one row");

        // Column names check
        assertEquals("Payslip No", model.getColumnName(0));
        assertEquals("Employee ID", model.getColumnName(1));
        assertEquals("Employee Name", model.getColumnName(2));
        assertEquals("Position/Department", model.getColumnName(3));
        assertEquals("Gross Income", model.getColumnName(4));
        assertEquals("Contributions", model.getColumnName(5));
        assertEquals("Deductions", model.getColumnName(6));
        assertEquals("Net Pay", model.getColumnName(7));
    }


    @Test
    void testDateChooserFilterForJune2024() throws Exception {
        JTable payrollTable         = getPrivateField(page, "payrollTable", JTable.class);
        com.toedter.calendar.JDateChooser JDateChooser = getPrivateField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);
        JTextField totalGrossField  = getPrivateField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = getPrivateField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField    = getPrivateField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField        = getPrivateField(page, "totalNetPayField", JTextField.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");

        SwingUtilities.invokeAndWait(() -> {
            JDateChooser.setDate(june3);
        });

        // Wait for table data to refresh (up to 2s)
        int retries = 0;
        while (payrollTable.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }

        TableModel model = payrollTable.getModel();
        assertTrue(model.getRowCount() > 0, "Rows present for June 2024");

        // Check summary fields reflect correct format
        assertFalse(totalGrossField.getText().isEmpty(), "Total Gross populated");
        assertFalse(totalContributionsField.getText().isEmpty(), "Total Contributions populated");
        assertFalse(totalDeductionsField.getText().isEmpty(), "Total Deductions populated");
        assertFalse(totalNetPayField.getText().isEmpty(), "Total Net Pay populated");

        // Validate numeric format
        assertTrue(totalGrossField.getText().matches("[\\d,]+\\.\\d{2}"), "Gross format ok");
        assertTrue(totalNetPayField.getText().matches("[\\d,]+\\.\\d{2}"), "Net pay format ok");
    }

    @Test
    void testBackButtonHasActionListener() {
        JButton backButton = getPrivateField(page, "backButton", JButton.class);
        ActionListener[] listeners = backButton.getActionListeners();
        assertTrue(listeners.length > 0, "Back button should have action listeners");
    }

    @Test
    void testPrintPayrollButtonTriggersAction() throws Exception {
        JButton printPayrollButton = getPrivateField(page, "printPayrollButton", JButton.class);
        com.toedter.calendar.JDateChooser JDateChooser = getPrivateField(page, "JDateChooser", com.toedter.calendar.JDateChooser.class);

        // Ensure at least one payslip is present for June 2024
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date june3 = sdf.parse("2024-06-03");
        SwingUtilities.invokeAndWait(() -> JDateChooser.setDate(june3));

        // ActionListener is attached
        ActionListener[] listeners = printPayrollButton.getActionListeners();
        assertTrue(listeners.length > 0, "Print Payroll button has action listener");

        // Simulate click: just ensure no exceptions (PDF generation handled in UI class)
        SwingUtilities.invokeAndWait(() -> {
            for (ActionListener l : listeners) {
                l.actionPerformed(new ActionEvent(printPayrollButton, ActionEvent.ACTION_PERFORMED, "click"));
            }
        });
    }

    @Test
    void testSummaryFieldsAreNonEditable() {
        JTextField totalGrossField = getPrivateField(page, "totalGrossField", JTextField.class);
        JTextField totalContributionsField = getPrivateField(page, "totalContributionsField", JTextField.class);
        JTextField totalDeductionsField = getPrivateField(page, "totalDeductionsField", JTextField.class);
        JTextField totalNetPayField = getPrivateField(page, "totalNetPayField", JTextField.class);

        assertFalse(totalGrossField.isEditable(), "Gross is non-editable");
        assertFalse(totalContributionsField.isEditable(), "Contributions non-editable");
        assertFalse(totalDeductionsField.isEditable(), "Deductions non-editable");
        assertFalse(totalNetPayField.isEditable(), "Net Pay non-editable");
    }
}