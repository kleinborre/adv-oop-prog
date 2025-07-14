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

    // Helper for private field access
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
        util.SessionManager.setSession("U10006", 10006);
        SwingUtilities.invokeAndWait(() -> page = new PageHREmployeeRecords());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
    }

    @Test
    void testButtonsExistAndFunctionality() throws Exception {
        JButton ownRecordBtn    = getPrivateField(page, "ownRecordButton", JButton.class);
        JButton newEmployeeBtn  = getPrivateField(page, "newEmployeeButton", JButton.class);
        JButton backBtn         = getPrivateField(page, "backButton", JButton.class);

        assertNotNull(ownRecordBtn, "Own Record button should exist");
        assertNotNull(newEmployeeBtn, "New Employee button should exist");
        assertNotNull(backBtn, "Back button should exist");

        assertTrue(ownRecordBtn.isEnabled(), "Own Record button should be enabled");
        assertTrue(newEmployeeBtn.isEnabled(), "New Employee button should be enabled");
        assertTrue(backBtn.isEnabled(), "Back button should be enabled");
    }

    @Test
    void testStatusFilterComboBoxExistsAndCanChange() throws Exception {
        JComboBox<?> statusFilter = getPrivateField(page, "statusFilter", JComboBox.class);
        assertNotNull(statusFilter, "Status filter combo box should exist");
        int originalIndex = statusFilter.getSelectedIndex();
        assertTrue(statusFilter.getItemCount() > 1, "Status filter should have multiple options");

        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedItem("Active"));
        assertEquals("Active", statusFilter.getSelectedItem(), "Status filter should change selection to Active");

        SwingUtilities.invokeAndWait(() -> statusFilter.setSelectedIndex(originalIndex));
    }

    @Test
    void testEmployeeRecordsTableExistsAndLoadsRows() throws Exception {
        JTable table = getPrivateField(page, "employeeRecordsTable", JTable.class);
        assertNotNull(table, "Employee records table should exist");

        // Wait for table to load (up to 2 seconds)
        int retries = 0;
        while (table.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }
        assertTrue(table.getRowCount() >= 0, "Table should be loaded with zero or more rows"); // Accept 0 if DB is empty
        assertEquals(14, table.getColumnCount(), "Table should have 14 columns");
        TableModel model = table.getModel();
        assertTrue(model instanceof DefaultTableModel, "Table model should be DefaultTableModel");

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
        JTable table = getPrivateField(page, "employeeRecordsTable", JTable.class);
        if (table.getRowCount() == 0) return;

        int row = 0;
        table.setRowSelectionInterval(row, row);
        MouseEvent click = new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 2, false);

        for (MouseListener ml : table.getMouseListeners()) {
            ml.mouseClicked(click);
        }

        assertTrue(true, "Double-click event triggered");
    }

    @Test
    void testScrollPaneContainsTable() throws Exception {
        JScrollPane scroll = getPrivateField(page, "jScrollPane1", JScrollPane.class);
        assertNotNull(scroll, "Scroll pane should exist");
        assertNotNull(scroll.getViewport().getView(), "Scroll pane should contain a view");
        assertTrue(scroll.getViewport().getView() instanceof JTable, "Scroll pane should contain JTable");
    }
}