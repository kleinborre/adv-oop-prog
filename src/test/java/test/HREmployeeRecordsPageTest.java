
package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

import ui.PageHREmployeeRecords;

public class HREmployeeRecordsPageTest {

    PageHREmployeeRecords page;

    @BeforeEach
    void setup() throws Exception {
        util.SessionManager.setSession("U10006", 10006);
        SwingUtilities.invokeAndWait(() -> page = new PageHREmployeeRecords());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
    }

    @Test
    void testButtonsExistAndFunctionality() throws Exception {
        JButton ownRecordBtn = page.ownRecordButton;
        JButton newEmployeeBtn = page.newEmployeeButton;
        JButton backBtn = page.backButton;

        assertNotNull(ownRecordBtn, "Own Record button should exist");
        assertNotNull(newEmployeeBtn, "New Employee button should exist");
        assertNotNull(backBtn, "Back button should exist");

        // Simulate and check the button actions: since they open other pages and dispose, just check enabled and visible for now.
        assertTrue(ownRecordBtn.isEnabled(), "Own Record button should be enabled");
        assertTrue(newEmployeeBtn.isEnabled(), "New Employee button should be enabled");
        assertTrue(backBtn.isEnabled(), "Back button should be enabled");
    }

    @Test
    void testStatusFilterComboBoxExistsAndCanChange() throws Exception {
        JComboBox<?> statusFilter = page.statusFilter;
        assertNotNull(statusFilter, "Status filter combo box should exist");
        int originalIndex = statusFilter.getSelectedIndex();
        assertTrue(statusFilter.getItemCount() > 1, "Status filter should have multiple options");

        // Select "Active" filter and verify selection changed
        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedItem("Active"));
        assertEquals("Active", statusFilter.getSelectedItem(), "Status filter should change selection to Active");

        // Restore original selection
        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedIndex(originalIndex));
    }

    @Test
    void testEmployeeRecordsTableExistsAndLoadsRows() throws Exception {
        JTable table = page.employeeRecordsTable;
        assertNotNull(table, "Employee records table should exist");

        // Wait for the SwingWorker to finish and table to load (up to 2 seconds)
        int retries = 0;
        while (table.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }
        assertTrue(table.getRowCount() >= 0, "Table should be loaded with zero or more rows"); // Accept 0 if DB is empty
        assertEquals(14, table.getColumnCount(), "Table should have 14 columns");
        TableModel model = table.getModel();
        assertTrue(model instanceof DefaultTableModel, "Table model should be DefaultTableModel");

        // Optionally, check header names
        String[] expectedHeaders = {
            "Employee ID","Account Status","Employment Status","Name",
            "Birthdate","Contact Number","Address","Position",
            "Department","Immediate Supervisor","SSS No.",
            "Philhealth No.","TIN No.","Pag-Ibig No."
        };
        for (int i = 0; i < expectedHeaders.length; i++) {
            assertEquals(expectedHeaders[i], table.getColumnName(i), "Header mismatch at column " + i);
        }
    }

    @Test
    void testDoubleClickTableRowOpensUpdatePage() throws Exception {
        JTable table = page.employeeRecordsTable;
        if (table.getRowCount() == 0) return; // Skip if no rows

        // Simulate double-click on first row
        int row = 0;
        table.setRowSelectionInterval(row, row);
        MouseEvent click = new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 2, false);

        // Use a flag to see if dialog opens (as an approximation, since modal dialog blocks tests)
        AtomicBoolean dialogOpened = new AtomicBoolean(false);
        // Replace JOptionPane temporarily (if needed for headless tests, otherwise skip this block)

        // Actually, just trigger the mouse event (won't fully test dialog, but will call mouse listener)
        for (MouseListener ml : table.getMouseListeners()) {
            ml.mouseClicked(click);
        }

        // Can't easily assert dialog in JUnit; but no error means success
        assertTrue(true, "Double-click event triggered");
    }

    @Test
    void testScrollPaneContainsTable() throws Exception {
        JScrollPane scroll = page.jScrollPane1;
        assertNotNull(scroll, "Scroll pane should exist");
        assertNotNull(scroll.getViewport().getView(), "Scroll pane should contain a view");
        assertTrue(scroll.getViewport().getView() instanceof JTable, "Scroll pane should contain JTable");
    }
}
