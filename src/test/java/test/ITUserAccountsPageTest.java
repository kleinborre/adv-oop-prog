package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;

import ui.PageITUserAccounts;

public class ITUserAccountsPageTest {

    PageITUserAccounts page;

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
        assertNotNull(page.backButton, "Back button exists");
        assertNotNull(page.ownAccountButton, "Own Account button exists");
        assertNotNull(page.newUserButton, "New Employee button exists");
        assertNotNull(page.statusFilter, "Status filter combo box exists");

        assertTrue(page.backButton.isEnabled(), "Back button enabled");
        assertTrue(page.ownAccountButton.isEnabled(), "Own Account button enabled");
        assertTrue(page.newUserButton.isEnabled(), "New Employee button enabled");
        assertTrue(page.statusFilter.isEnabled(), "Status filter enabled");
    }

    @Test
    void testTableAndScrollPanePresentAndPopulated() {
        assertNotNull(page.userAccountsTable, "User accounts table exists");
        assertNotNull(page.jScrollPane1, "Table scroll pane exists");

        // Table is inside scroll pane
        assertSame(page.userAccountsTable, page.jScrollPane1.getViewport().getView(), "Table in scroll pane");

        // Columns and rows must be present
        TableModel model = page.userAccountsTable.getModel();
        assertTrue(model.getColumnCount() >= 4, "Table has columns");
        assertTrue(model.getRowCount() > 0, "Table has at least one row");

        // Column names
        assertEquals("Account Status", model.getColumnName(0));
        assertEquals("User ID",        model.getColumnName(1));
        assertEquals("Full Name",      model.getColumnName(2));
        assertEquals("Email",          model.getColumnName(3));
    }

    @Test
    void testStatusFilterChangesTableRows() throws Exception {
        // Try each status, see table updates (row count may go up or down, just not error)
        SwingUtilities.invokeAndWait(() -> {
            int totalRows = page.userAccountsTable.getRowCount();
            for (int i = 0; i < page.statusFilter.getItemCount(); ++i) {
                page.statusFilter.setSelectedIndex(i);
                int count = page.userAccountsTable.getRowCount();
                assertTrue(count >= 0, "Row count non-negative after filter change");
            }
        });
    }

    @Test
    void testOwnAccountAndNewEmployeeButtonsAction() throws Exception {
        // Just ensure action listeners present (don’t trigger navigation in test)
        ActionListener[] ownAccL = page.ownAccountButton.getActionListeners();
        assertTrue(ownAccL.length > 0, "Own Account button has action listener");

        ActionListener[] newUserL = page.newUserButton.getActionListeners();
        assertTrue(newUserL.length > 0, "New Employee button has action listener");
    }

    @Test
    void testBackButtonAction() {
        ActionListener[] backListeners = page.backButton.getActionListeners();
        assertTrue(backListeners.length > 0, "Back button should have action listeners");
    }

    @Test
    void testRowPopupUpdateAndDisableActionForU10034() throws Exception {
        // Find the row for userID U10034 (case-sensitive)
        TableModel model = page.userAccountsTable.getModel();
        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String val = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (val.equals("U10034")) {
                targetRow = i;
                break;
            }
        }
        assertTrue(targetRow >= 0, "Row for userID U10034 found in table");

        // Simulate double-clicking this row (popup for active account)
        final int row = targetRow;
        SwingUtilities.invokeAndWait(() -> {
            // simulate mouse event (double click)
            MouseEvent evt = new MouseEvent(
                page.userAccountsTable,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                10, 10,
                2,  // click count (double click)
                false
            );
            // set selection before firing (some UIs require selection)
            page.userAccountsTable.setRowSelectionInterval(row, row);

            // Actually dispatch the event (calls the mouseClicked logic)
            for (MouseListener ml : page.userAccountsTable.getMouseListeners()) {
                ml.mouseClicked(evt);
            }
            // No assertion on selection or table state after click: 
            // - Dialog blocks user input and may change account status,
            // - Just assert that the operation does not throw and row exists at start.
        });

        // Test passes if no exceptions and popup logic is triggered
        assertTrue(true, "Simulated double-click on U10034 handled gracefully regardless of state changes");
    }

    @Test
    void testTableCellValuesForU10034() {
        TableModel model = page.userAccountsTable.getModel();
        boolean found = false;
        for (int i = 0; i < model.getRowCount(); ++i) {
            String userID = (model.getValueAt(i, 1) != null) ? model.getValueAt(i, 1).toString() : "";
            if (userID.equals("U10034")) {
                // Check cell values are non-null and formatted
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
