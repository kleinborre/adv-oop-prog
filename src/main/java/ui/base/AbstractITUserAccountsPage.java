package ui.base;

import pojo.User;
import pojo.Employee;
import service.UserService;
import service.EmployeeService;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractITUserAccountsPage extends JFrame {

    protected JTable userAccountsTable;
    protected JComboBox<String> statusFilter;
    protected JButton ownAccountButton;
    protected JButton newUserButton;
    protected JButton backButton;

    protected UserService userService = new UserService();
    protected EmployeeService employeeService = new EmployeeService();

    private final String[] columnNames = {
            "Account Status", "User ID", "Full Name", "Email"
    };

    // Must be called after UI's initComponents (fields must be non-null!)
    protected void setupUserAccountsPage(JTable userAccountsTable, JComboBox<String> statusFilter,
                                         JButton ownAccountButton, JButton newUserButton, JButton backButton) {
        this.userAccountsTable = userAccountsTable;
        this.statusFilter = statusFilter;
        this.ownAccountButton = ownAccountButton;
        this.newUserButton = newUserButton;
        this.backButton = backButton;

        populateUserAccountsTable(getCurrentFilter());
        setupStatusFilterListener();
        setupRowDoubleClickListener();

        ownAccountButton.addActionListener(e -> {
            new ui.PageITEmployeeData().setVisible(true);
            this.dispose();
        });

        newUserButton.addActionListener(e -> {
            new ui.PageITEmployeeRegister().setVisible(true);
            this.dispose();
        });

        backButton.addActionListener(e -> {
            new ui.PageITHome().setVisible(true);
            this.dispose();
        });
    }

    protected String getCurrentFilter() {
        if (statusFilter == null) return "All";
        Object selected = statusFilter.getSelectedItem();
        return selected == null ? "All" : selected.toString();
    }

    protected void populateUserAccountsTable(String filter) {
        List<User> users = userService.getAllUsers();
        // Sort by userID ascending (U00001, U00002, ...)
        List<User> sortedUsers = users.stream()
            .filter(u -> filter.equals("All") || u.getAccountStatus().equalsIgnoreCase(filter))
            .sorted(Comparator.comparing(User::getUserID))
            .collect(Collectors.toList());

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (User u : sortedUsers) {
            Employee emp = employeeService.getEmployeeByUserID(u.getUserID());
            String fullName = (emp != null) ? emp.getLastName() + ", " + emp.getFirstName() : "";
            String email = (emp != null) ? emp.getEmail() : "";
            model.addRow(new Object[]{u.getAccountStatus(), u.getUserID(), fullName, email});
        }
        userAccountsTable.setModel(model);

        // Center the Account Status and User ID columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        userAccountsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userAccountsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }

    private void setupStatusFilterListener() {
        statusFilter.addActionListener(e -> populateUserAccountsTable(getCurrentFilter()));
    }

    private void setupRowDoubleClickListener() {
        userAccountsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = userAccountsTable.rowAtPoint(evt.getPoint());
                if (row < 0) return;
                if (evt.getClickCount() == 2) {
                    String accountStatus = userAccountsTable.getValueAt(row, 0).toString();
                    String userID = userAccountsTable.getValueAt(row, 1).toString();
                    handleAccountRowAction(accountStatus, userID);
                }
            }
        });
    }

    // Handler for double-click based on status
    private void handleAccountRowAction(String accountStatus, String userID) {
        switch (accountStatus) {
            case "Pending":
                handlePendingAccount(userID);
                break;
            case "Active":
                handleActiveAccount(userID);
                break;
            case "Deactivated":
                handleDeactivatedAccount(userID);
                break;
            case "Rejected":
                JOptionPane.showMessageDialog(this, "This account has been rejected. No further action possible.");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown account status.");
        }
    }

    // For Pending accounts: Accept or Reject (No Cancel)
    private void handlePendingAccount(String userID) {
        String[] options = {"Accept", "Reject"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose action for pending account:",
                "Account Approval",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) {
            updateAccountStatus(userID, "Active");
            JOptionPane.showMessageDialog(this, "Account approved. User can now log in.");
        } else if (choice == 1) {
            updateAccountStatus(userID, "Rejected");
            JOptionPane.showMessageDialog(this, "Account rejected. User cannot log in.");
        }
        populateUserAccountsTable(getCurrentFilter());
    }

    // For Active accounts: Update or Disable (No Cancel)
    private void handleActiveAccount(String userID) {
        String[] options = {"Update Account", "Disable Account"};
        int choice = JOptionPane.showOptionDialog(this,
                "What would you like to do with this active account?",
                "Manage Active Account",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) {
            Employee emp = employeeService.getEmployeeByUserID(userID);
            if (emp != null) {
                SessionManager.setSelectedEmployeeID(emp.getEmployeeID());
                new ui.PageITEmployeeUpdate().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Employee not found for this user.");
            }
        } else if (choice == 1) {
            updateAccountStatus(userID, "Deactivated");
            JOptionPane.showMessageDialog(this, "Account has been deactivated.");
            populateUserAccountsTable(getCurrentFilter());
        }
    }

    // For Deactivated accounts: Reactivate (Only Yes/No)
    private void handleDeactivatedAccount(String userID) {
        String[] options = {"Reactivate", "Close"};
        int confirm = JOptionPane.showOptionDialog(this,
                "This account is deactivated. Reactivate account?",
                "Reactivate Account",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (confirm == 0) {
            updateAccountStatus(userID, "Active");
            JOptionPane.showMessageDialog(this, "Account has been reactivated.");
            populateUserAccountsTable(getCurrentFilter());
        }
    }

    // DRY: All account status updates in one place
    protected void updateAccountStatus(String userID, String newStatus) {
        User user = userService.getUserByUserID(userID);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }
        user.setAccountStatus(newStatus);
        userService.updateUser(user);
    }
}
