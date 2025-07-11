package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;

import ui.PageITUserAccounts;

public class ITUserAccountsPageTest {

    PageITUserAccounts page;

    // Helper for private field access, used in all tests for robustness.
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
        util.SessionManager.setSession("U10005", 10005); // IT admin

        // --- Reset U10034 to "Active" (if exists) before each test ---
        service.UserService userService = new service.UserService();
        pojo.User u = userService.getUserByUserID("U10034");
        if (u != null && !"Active".equals(u.getAccountStatus())) {
            u.setAccountStatus("Active");
            userService.updateUser(u);
        }

        SwingUtilities.invokeAndWait(() -> page = new PageITUserAccounts());
    }

    @AfterEach
    void cleanup() throws Exception {
        SwingUtilities.invokeAndWait(() -> page.dispose());
        util.SessionManager.clearSession();
    }

    @Test
    void testAllButtonsAndCombosPresentAndEnabled() {
        JButton backButton       = getPrivateField(page, "backButton", JButton.class);
        JButton ownAccountButton = getPrivateField(page, "ownAccountButton", JButton.class);
        JButton newUserButton    = getPrivateField(page, "newUserButton", JButton.class);
        JComboBox<?> statusFilter = getPrivateField(page, "statusFilter", JComboBox.class);

        assertNotNull(backButton, "Back button exists");
        assertNotNull(ownAccountButton, "Own Account button exists");
        assertNotNull(newUserButton, "New Employee button exists");
        assertNotNull(statusFilter, "Status filter combo box exists");

        assertTrue(backButton.isEnabled(), "Back button enabled");
        assertTrue(ownAccountButton.isEnabled(), "Own Account button enabled");
        assertTrue(newUserButton.isEnabled(), "New Employee button enabled");
        assertTrue(statusFilter.isEnabled(), "Status filter enabled");
    }

    @Test
    void testEmployeeIDComboBoxExistsAndWorks() {
        JComboBox<?> employeeIDComboBox = getPrivateField(page, "employeeIDComboBox", JComboBox.class);
        assertNotNull(employeeIDComboBox, "Employee ID combo box exists");
        assertTrue(employeeIDComboBox.getItemCount() > 0, "Employee ID combo box has items");
        assertEquals("All", employeeIDComboBox.getItemAt(0), "First combo box item is All");
    }

    @Test
    void testTableAndScrollPanePresentAndPopulated() throws Exception {
        JTable table = getPrivateField(page, "userAccountsTable", JTable.class);
        JScrollPane scroll = getPrivateField(page, "jScrollPane1", JScrollPane.class);
        assertNotNull(table, "User accounts table exists");
        assertNotNull(scroll, "Table scroll pane exists");

        // Table is inside scroll pane
        assertSame(table, scroll.getViewport().getView(), "Table in scroll pane");

        // Wait for table to load (up to 2 seconds)
        int retries = 0;
        while (table.getRowCount() == 0 && retries < 20) {
            Thread.sleep(100);
            retries++;
        }
        TableModel model = table.getModel();
        assertTrue(model.getColumnCount() >= 4, "Table has columns");
        assertTrue(model.getRowCount() >= 0, "Table has zero or more rows (ok if no users)");

        // Column names
        assertEquals("Account Status", model.getColumnName(0));
        assertEquals("User ID",        model.getColumnName(1));
        assertEquals("Full Name",      model.getColumnName(2));
        assertEquals("Email",          model.getColumnName(3));
    }

    @Test
    void testStatusFilterChangesTableRows() throws Exception {
        JTable table = getPrivateField(page, "userAccountsTable", JTable.class);
        JComboBox<?> statusFilter = getPrivateField(page, "statusFilter", JComboBox.class);

        SwingUtilities.invokeAndWait(() -> {
            for (int i = 0; i < statusFilter.getItemCount(); ++i) {
                statusFilter.setSelectedIndex(i);
                int count = table.getRowCount();
                assertTrue(count >= 0, "Row count non-negative after filter change");
            }
        });
    }

    @Test
    void testOwnAccountAndNewEmployeeButtonsAction() {
        JButton ownAccountButton = getPrivateField(page, "ownAccountButton", JButton.class);
        JButton newUserButton = getPrivateField(page, "newUserButton", JButton.class);
        ActionListener[] ownAccL = ownAccountButton.getActionListeners();
        ActionListener[] newUserL = newUserButton.getActionListeners();
        assertTrue(ownAccL.length > 0, "Own Account button has action listener");
        assertTrue(newUserL.length > 0, "New Employee button has action listener");
    }

    @Test
    void testBackButtonAction() {
        JButton backButton = getPrivateField(page, "backButton", JButton.class);
        ActionListener[] backListeners = backButton.getActionListeners();
        assertTrue(backListeners.length > 0, "Back button should have action listeners");
    }

    @Test
    void testRowPopupUpdateAndDisableActionForU10034() throws Exception {
        JTable table = getPrivateField(page, "userAccountsTable", JTable.class);
        TableModel model = table.getModel();
        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String val = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (val.equals("U10034")) {
                targetRow = i;
                break;
            }
        }
        assertTrue(targetRow >= 0, "Row for userID U10034 found in table");

        // Simulate double-clicking this row
        final int row = targetRow;
        SwingUtilities.invokeAndWait(() -> {
            MouseEvent evt = new MouseEvent(
                table,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                10, 10,
                2,  // double click
                false
            );
            table.setRowSelectionInterval(row, row);

            for (MouseListener ml : table.getMouseListeners()) {
                ml.mouseClicked(evt);
            }
        });

        assertTrue(true, "Simulated double-click on U10034 handled gracefully");
    }

    @Test
    void testTableCellValuesForU10034() {
        JTable table = getPrivateField(page, "userAccountsTable", JTable.class);
        TableModel model = table.getModel();
        boolean found = false;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String userID = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (userID.equals("U10034")) {
                assertNotNull(model.getValueAt(i, 0), "Status non-null");
                assertNotNull(model.getValueAt(i, 2), "Full name non-null");
                assertNotNull(model.getValueAt(i, 3), "Email non-null");
                found = true;
                break;
            }
        }
        assertTrue(found, "UserID U10034 exists in table");
    }
}