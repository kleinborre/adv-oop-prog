package service;

import dao.EmployeeDAO;
import daoimpl.EmployeeDAOImpl;
import pojo.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import db.DatabaseConnection;
import java.util.List;

public class EmployeeService {

    private EmployeeDAO employeeDAO;

    public EmployeeService() {
        try {
            employeeDAO = new EmployeeDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing EmployeeDAO", e);
        }
    }

    public Employee getEmployeeByID(int employeeID) {
        String sql =
          "SELECT e.*, " +
          "       p.position, " +
          "       s.statusType   AS statusDesc, " +
          "       g.sss          AS sssNo, " +
          "       g.pagibig      AS pagibigNo, " +
          "       g.philhealth   AS philhealthNo, " +
          "       g.tin          AS tinNo, " +
          "       a.addressID, " +
          "       a.houseNo, a.street, " +
          "       a.barangay, a.city, a.province, a.zipCode " +
          "  FROM employee e " +
          "  JOIN position p          ON e.positionID   = p.positionID " +
          "  JOIN status s            ON e.statusID     = s.statusID " +
          "  JOIN employeegovid eg    ON e.employeeID   = eg.employeeID " +
          "  JOIN govid g             ON eg.govID       = g.govID " +
          "  JOIN employeeaddress ea  ON e.employeeID   = ea.employeeID " +
          "  JOIN address a           ON ea.addressID   = a.addressID " +
          " WHERE e.employeeID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Employee e = new Employee();
                e.setEmployeeID(rs.getInt("employeeID"));
                e.setFirstName (rs.getString("firstName"));
                e.setLastName  (rs.getString("lastName"));
                e.setBirthDate (rs.getDate("birthDate"));
                e.setPhoneNo   (rs.getString("phoneNo"));
                e.setEmail     (rs.getString("email"));
                e.setUserID    (rs.getString("userID"));
                e.setStatusID  (rs.getInt("statusID"));
                e.setPositionID(rs.getInt("positionID"));
                e.setDepartmentID(rs.getInt("departmentID"));
                e.setSupervisorID(rs.getInt("supervisorID"));

                // transient fields
                e.setPosition      (rs.getString("position"));
                e.setStatusDesc    (rs.getString("statusDesc"));
                e.setSssNo         (rs.getString("sssNo"));
                e.setPagibigNo     (rs.getString("pagibigNo"));
                e.setPhilhealthNo  (rs.getString("philhealthNo"));
                e.setTinNo         (rs.getString("tinNo"));

                // address parts
                e.setAddressID(rs.getInt("addressID"));
                e.setHouseNo  (rs.getString("houseNo"));
                e.setStreet   (rs.getString("street"));
                e.setBarangay (rs.getString("barangay"));
                e.setCity     (rs.getString("city"));
                e.setProvince (rs.getString("province"));
                e.setZipCode  (rs.getInt("zipCode"));

                // supervisor name
                int supID = e.getSupervisorID();
                if (supID != 0 && supID != e.getEmployeeID()) {
                  Employee sup = getEmployeeByID(supID);
                  e.setSupervisorName(sup == null
                      ? "No Supervisor"
                      : sup.getLastName()+", "+sup.getFirstName());
                } else {
                  e.setSupervisorName("No Supervisor");
                }

                return e;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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
    
    public void updateEmployeeProfile(Employee e) {
        String updEmp = "UPDATE employee SET phoneNo=? WHERE employeeID=?";
        String updAddr =
          "UPDATE address a " +
          "  JOIN employeeaddress ea ON a.addressID = ea.addressID " +
          " SET a.houseNo = ?, a.street = ?, a.barangay = ?, " +
          "     a.city    = ?, a.province = ?, a.zipCode = ? " +
          "WHERE ea.employeeID = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(updEmp);
                 PreparedStatement p2 = conn.prepareStatement(updAddr)) {

                // 1) update phone
                p1.setString(1, e.getPhoneNo());
                p1.setInt   (2, e.getEmployeeID());
                p1.executeUpdate();

                // 2) update address parts
                p2.setString(1, e.getHouseNo());
                p2.setString(2, e.getStreet());
                p2.setString(3, e.getBarangay());
                p2.setString(4, e.getCity());
                p2.setString(5, e.getProvince());
                p2.setInt   (6, e.getZipCode());
                p2.setInt   (7, e.getEmployeeID());
                p2.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}