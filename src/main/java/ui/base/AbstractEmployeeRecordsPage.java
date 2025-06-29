package ui.base;

import util.LightButton;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
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

    // re-attach header (NetBeans cleared it by default):
    Container vp = table.getParent();
    if (vp instanceof JViewport) {
      Container sp = vp.getParent();
      if (sp instanceof JScrollPane) {
        ((JScrollPane)sp).setColumnHeaderView(table.getTableHeader());
      }
    }

    // 2) Always allow horizontal scrolling:
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // 3) Center everything:
    DefaultTableCellRenderer center = new DefaultTableCellRenderer();
    center.setHorizontalAlignment(SwingConstants.CENTER);
    table.setDefaultRenderer(Object.class, center);
    JTableHeader hdr = table.getTableHeader();
    ((DefaultTableCellRenderer)hdr.getDefaultRenderer())
      .setHorizontalAlignment(SwingConstants.CENTER);

    // 4) Wire filter & buttons:
    statusFilter.addActionListener(e ->
      reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()))
    );
    ownRecordButton   .addActionListener(e -> onOwnRecord.run());
    newEmployeeButton .addActionListener(e -> onNewEmployee.run());
    backButton        .addActionListener(e -> onBack.run());

    // 5) First load:
    reloadTableAsync(table, String.valueOf(statusFilter.getSelectedItem()));
  }

  /**
   * Fetches in background so UI stays responsive, then repopulates & auto-resizes cols.
   */
  private void reloadTableAsync(JTable table, String filterStatus) {
    new SwingWorker<List<Object[]>,Void>() {
      @Override protected List<Object[]> doInBackground() {
        return employeeService.getAllEmployeeRecords(filterStatus);
      }
      @Override protected void done() {
        try {
          List<Object[]> rows = get();
          DefaultTableModel m = (DefaultTableModel)table.getModel();
          m.setRowCount(0);
          for (Object[] row : rows) {
            m.addRow(row);
          }
          // **NEW**: adjust each column to fit its widest cell/header
          adjustColumnWidths(table);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }.execute();
  }

  /**
   * Scans each column’s header + all its cells, then sets that column’s preferred width
   * to the maximum preferred width found.  Adds a small padding.
   */
  private void adjustColumnWidths(JTable table) {
    TableColumnModel colModel = table.getColumnModel();
    JTableHeader hdr = table.getTableHeader();
    TableCellRenderer hdrRend = hdr.getDefaultRenderer();

    for (int col = 0; col < table.getColumnCount(); col++) {
      int maxW = 50;  // a reasonable minimum
      // check header
      Component hComp = hdrRend.getTableCellRendererComponent(
        table,
        colModel.getColumn(col).getHeaderValue(),
        false, false,
        -1, col
      );
      maxW = Math.max(maxW, hComp.getPreferredSize().width);

      // check each row
      for (int row = 0; row < table.getRowCount(); row++) {
        TableCellRenderer cellRend = table.getCellRenderer(row, col);
        Component c = table.prepareRenderer(cellRend, row, col);
        maxW = Math.max(maxW, c.getPreferredSize().width);
      }

      // set with a bit of padding
      colModel.getColumn(col).setPreferredWidth(maxW + 10);
    }
  }
}