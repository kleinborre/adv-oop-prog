package ui.base;

import util.LightButton;
import util.SessionManager;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public abstract class AbstractEmployeeRecordsPage extends JFrame {
  private final EmployeeService employeeService = new EmployeeService();

  /**
   * Call once in your UI constructor (after initComponents()):
   */
  protected void setupRecordsPage(
    JTable      table,
    JComboBox<?> statusFilter,
    LightButton ownRecordButton,
    LightButton newEmployeeButton,
    LightButton backButton,
    Runnable    onOwnRecord,
    Runnable    onNewEmployee,
    Runnable    onBack
  ) {
    // 1) Build a 14-column, 0-row model:
    String[] cols = {
      "Employee ID","Account Status","Employment Status","Name",
      "Birthdate","Contact Number","Address","Position",
      "Department","Immediate Supervisor","SSS No.",
      "Philhealth No.","TIN No.","Pag-Ibig No."
    };
    DefaultTableModel model = new DefaultTableModel(cols, 0) {
      @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    table.setModel(model);
    table.createDefaultColumnsFromModel();

    // re-attach header if needed
    Container vp = table.getParent();
    if (vp instanceof JViewport) {
      Container sp = vp.getParent();
      if (sp instanceof JScrollPane) {
        ((JScrollPane)sp).setColumnHeaderView(table.getTableHeader());
      }
    }

    // 2) horizontal scrolling
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // 3) center everything
    DefaultTableCellRenderer center = new DefaultTableCellRenderer();
    center.setHorizontalAlignment(SwingConstants.CENTER);
    table.setDefaultRenderer(Object.class, center);
    JTableHeader hdr = table.getTableHeader();
    ((DefaultTableCellRenderer)hdr.getDefaultRenderer())
      .setHorizontalAlignment(SwingConstants.CENTER);

    // 4) wire filter & buttons
    statusFilter.addActionListener(e ->
      reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()))
    );
    ownRecordButton  .addActionListener(e -> onOwnRecord.run());
    newEmployeeButton.addActionListener(e -> onNewEmployee.run());
    backButton       .addActionListener(e -> onBack.run());

    // 5) initial load
    reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));

    // 6) double-click -> update flow
    table.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
          int row = table.getSelectedRow();
          // column 0 is employeeID
          int empID = (Integer)table.getModel().getValueAt(row, 0);

          int choice = JOptionPane.showConfirmDialog(
            AbstractEmployeeRecordsPage.this,
            "Do you want to update this employee?",
            "Update Employee",
            JOptionPane.YES_NO_OPTION
          );
          if (choice == JOptionPane.YES_OPTION) {
            SessionManager.setSelectedEmployeeID(empID);
            new ui.PageHREmployeeUpdate().setVisible(true);
            dispose();
          }
        }
      }
    });
  }

  private void reloadTableAsync(JTable table, String filterStatus) {
    new SwingWorker<List<Object[]>,Void>() {
      @Override protected List<Object[]> doInBackground() {
        return employeeService.getAllEmployeeRecords(filterStatus);
      }
      @Override protected void done() {
        try {
          var rows = get();
          var m = (DefaultTableModel)table.getModel();
          m.setRowCount(0);
          for (var row : rows) m.addRow(row);
          adjustColumnWidths(table);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }.execute();
  }

  private void adjustColumnWidths(JTable table) {
    TableColumnModel cm = table.getColumnModel();
    JTableHeader hdr = table.getTableHeader();
    TableCellRenderer hdrR = hdr.getDefaultRenderer();

    for (int col = 0; col < table.getColumnCount(); col++) {
      int maxW = 50;
      Component hc = hdrR.getTableCellRendererComponent(
        table, cm.getColumn(col).getHeaderValue(), false, false, -1, col
      );
      maxW = Math.max(maxW, hc.getPreferredSize().width);
      for (int row = 0; row < table.getRowCount(); row++) {
        TableCellRenderer cr = table.getCellRenderer(row, col);
        Component c = table.prepareRenderer(cr, row, col);
        maxW = Math.max(maxW, c.getPreferredSize().width);
      }
      cm.getColumn(col).setPreferredWidth(maxW + 10);
    }
  }
}