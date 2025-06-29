package service;

import dao.EmployeeDAO;
import daoimpl.EmployeeDAOImpl;
import pojo.Employee;

import db.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {

    private final EmployeeDAO employeeDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAOImpl();
    }

    public Employee getEmployeeByID(int employeeID) {
        String query =
            "SELECT e.*, " +
            "       p.position, " +
            "       s.statusType  AS statusDesc, " +
            "       g.sss         AS sssNo, " +
            "       g.pagibig     AS pagibigNo, " +
            "       g.philhealth  AS philhealthNo, " +
            "       g.tin         AS tinNo, " +
            "       a.houseNo, a.street, a.barangay, a.city, a.province, a.zipCode " +
            "  FROM employee e " +
            "  JOIN position p ON e.positionID = p.positionID " +
            "  JOIN status s   ON e.statusID   = s.statusID " +
            "  JOIN govid  g   ON e.employeeID = g.employeeID " +
            "  JOIN employeeaddress ea ON e.employeeID = ea.employeeID " +
            "  JOIN address a         ON ea.addressID   = a.addressID " +
            " WHERE e.employeeID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                Employee emp = new Employee();
                emp.setEmployeeID(   rs.getInt("employeeID"));
                emp.setFirstName(    rs.getString("firstName"));
                emp.setLastName(     rs.getString("lastName"));
                emp.setBirthDate(    rs.getDate("birthDate"));
                emp.setPhoneNo(      rs.getString("phoneNo"));
                emp.setEmail(        rs.getString("email"));
                emp.setUserID(       rs.getString("userID"));
                emp.setStatusID(     rs.getInt("statusID"));
                emp.setPositionID(   rs.getInt("positionID"));
                emp.setDepartmentID( rs.getInt("departmentID"));
                emp.setSupervisorID( rs.getInt("supervisorID"));

                // transient/profile fields
                emp.setPosition(     rs.getString("position"));
                emp.setStatusDesc(   rs.getString("statusDesc"));
                emp.setSssNo(        rs.getString("sssNo"));
                emp.setPagibigNo(    rs.getString("pagibigNo"));
                emp.setPhilhealthNo( rs.getString("philhealthNo"));
                emp.setTinNo(        rs.getString("tinNo"));

                // address parts
                emp.setHouseNo(  rs.getString("houseNo"));
                emp.setStreet(   rs.getString("street"));
                emp.setBarangay( rs.getString("barangay"));
                emp.setCity(     rs.getString("city"));
                emp.setProvince( rs.getString("province"));
                int z = rs.getInt("zipCode");
                emp.setZipCode(rs.wasNull() ? null : z);

                // supervisor name lookup
                int supId = emp.getSupervisorID();
                if (supId > 0) {
                    String supSql = "SELECT lastName, firstName FROM employee WHERE employeeID = ?";
                    try (PreparedStatement supStmt = conn.prepareStatement(supSql)) {
                        supStmt.setInt(1, supId);
                        try (ResultSet srs = supStmt.executeQuery()) {
                            if (srs.next()) {
                                emp.setSupervisorName(
                                    srs.getString("lastName") + ", " + srs.getString("firstName")
                                );
                            } else {
                                emp.setSupervisorName("No Supervisor");
                            }
                        }
                    }
                } else {
                    emp.setSupervisorName("No Supervisor");
                }

                return emp;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee by ID", e);
        }
    }

    public Employee getEmployeeByUserID(String userID) {
        try {
            return employeeDAO.getEmployeeByUserID(userID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving employee by userID", e);
        }
    }

    public List<Employee> getAllEmployees() {
        try {
            return employeeDAO.getAllEmployees();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all employees", e);
        }
    }

    public void addEmployee(Employee employee) {
        try {
            employeeDAO.addEmployee(employee);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding employee", e);
        }
    }

    public void updateEmployee(Employee employee) {
        try {
            employeeDAO.updateEmployee(employee);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee", e);
        }
    }

    public void deleteEmployee(int employeeID) {
        try {
            employeeDAO.deleteEmployee(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting employee", e);
        }
    }
    
    public String getDepartmentName(int departmentID) {
        String sql = "SELECT departmentName FROM department WHERE departmentID = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
          p.setInt(1, departmentID);
          try (ResultSet r = p.executeQuery()) {
            if (r.next()) return r.getString("departmentName");
          }
        } catch (SQLException e) {
          throw new RuntimeException("Error fetching department name", e);
        }
        return "";
    }

    /**
     * Fetches every employee’s full dashboard row—joins authentication,
     * position, status, govid, address, department, supervisor—all in one go,
     * optionally filtered by accountStatus (or "All").
     */
    public List<Object[]> getAllEmployeeRecords(String filterStatus) {
        String sql =
            "SELECT\n" +
            "  e.employeeID,\n" +
            "  a.accountStatus,\n" +
            "  s.statusType      AS employmentStatus,\n" +
            "  CONCAT(e.lastName, ', ', e.firstName) AS fullName,\n" +
            "  e.birthDate,\n" +
            "  e.phoneNo,\n" +
            "  -- new CONCAT_WS here:\n" +
            "  CONCAT_WS(\", \",\n" +
            "     CONCAT_WS(' ', a2.houseNo, a2.street),\n" +
            "     a2.barangay,\n" +
            "     a2.city,\n" +
            "     a2.province,\n" +
            "     COALESCE(CAST(a2.zipCode AS CHAR), '')\n" +
            "  ) AS address,\n" +
            "  p.position,\n" +
            "  d.departmentName,\n" +
            "  COALESCE(CONCAT(sup.lastName, ', ', sup.firstName),'No Supervisor') AS supervisorName,\n" +
            "  g.sss       AS sssNo,\n" +
            "  g.philhealth AS philhealthNo,\n" +
            "  g.tin       AS tinNo,\n" +
            "  g.pagibig   AS pagibigNo\n" +
            "FROM employee e\n" +
            "  JOIN authentication a    ON e.userID       = a.userID\n" +
            "  JOIN status s            ON e.statusID    = s.statusID\n" +
            "  JOIN position p          ON e.positionID  = p.positionID\n" +
            "  JOIN govid g             ON e.employeeID  = g.employeeID\n" +
            "  JOIN employeeaddress ea  ON e.employeeID  = ea.employeeID\n" +
            "  JOIN address a2          ON ea.addressID   = a2.addressID\n" +
            "  JOIN department d        ON e.departmentID= d.departmentID\n" +
            "  LEFT JOIN employee sup   ON e.supervisorID= sup.employeeID\n" +
            "WHERE (? = 'All' OR a.accountStatus = ?)";

        List<Object[]> rows = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            // bind filterStatus twice
            p.setString(1, filterStatus);
            p.setString(2, filterStatus);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Date bd = r.getDate("birthDate");
                    String bdText = bd!=null ? bd.toLocalDate().format(df) : "";
                    rows.add(new Object[]{
                        r.getInt("employeeID"),
                        r.getString("accountStatus"),
                        r.getString("employmentStatus"),
                        r.getString("fullName"),
                        bdText,
                        r.getString("phoneNo"),
                        r.getString("address"),    // ← now never NULL
                        r.getString("position"),
                        r.getString("departmentName"),
                        r.getString("supervisorName"),
                        r.getString("sssNo"),
                        r.getString("philhealthNo"),
                        r.getString("tinNo"),
                        r.getString("pagibigNo")
                    });
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error retrieving employee records", ex);
        }

        return rows;
    }
}