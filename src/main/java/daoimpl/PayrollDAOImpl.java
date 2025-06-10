package daoimpl;

import dao.PayrollDAO;
import db.DatabaseConnection;
import pojo.Payroll;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAOImpl implements PayrollDAO {

    private Connection connection;

    public PayrollDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Payroll getPayrollByID(int payrollID) throws SQLException {
        String query = "SELECT * FROM payroll WHERE payrollID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payrollID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayroll(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Payroll> getPayrollsByEmployeeID(int employeeID) throws SQLException {
        List<Payroll> payrollList = new ArrayList<>();
        String query = "SELECT * FROM payroll WHERE employeeID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payrollList.add(mapResultSetToPayroll(rs));
                }
            }
        }
        return payrollList;
    }

    @Override
    public List<Payroll> getAllPayrolls() throws SQLException {
        List<Payroll> payrollList = new ArrayList<>();
        String query = "SELECT * FROM payroll";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                payrollList.add(mapResultSetToPayroll(rs));
            }
        }
        return payrollList;
    }
    
    @Override
    public List<Payroll> getAllPayrollsByPeriod(int payPeriodID) throws SQLException {
        List<Payroll> payrollList = new ArrayList<>();
        String query = "SELECT * FROM payroll WHERE payPeriodID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payPeriodID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = new Payroll();
                    payroll.setPayrollID(rs.getInt("payrollID"));
                    payroll.setGrossPay(rs.getDouble("grossPay"));
                    payroll.setTotalDeductions(rs.getDouble("totalDeductions"));
                    payroll.setWithholdingTax(rs.getDouble("withholdingTax"));
                    payroll.setNetPay(rs.getDouble("netPay"));
                    payroll.setPayPeriodID(rs.getInt("payPeriodID"));
                    payroll.setEmployeeID(rs.getInt("employeeID"));
                    payrollList.add(payroll);
                }
            }
        }
        return payrollList;
    }

    @Override
    public void addPayroll(Payroll payroll) throws SQLException {
        String query = "INSERT INTO payroll (grossPay, totalDeductions, withholdingTax, netPay, payPeriodID, employeeID) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, payroll.getGrossPay());
            stmt.setDouble(2, payroll.getTotalDeductions());
            stmt.setDouble(3, payroll.getWithholdingTax());
            stmt.setDouble(4, payroll.getNetPay());
            stmt.setInt(5, payroll.getPayPeriodID());
            stmt.setInt(6, payroll.getEmployeeID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updatePayroll(Payroll payroll) throws SQLException {
        String query = "UPDATE payroll SET grossPay = ?, totalDeductions = ?, withholdingTax = ?, netPay = ?, payPeriodID = ?, employeeID = ? " +
                       "WHERE payrollID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, payroll.getGrossPay());
            stmt.setDouble(2, payroll.getTotalDeductions());
            stmt.setDouble(3, payroll.getWithholdingTax());
            stmt.setDouble(4, payroll.getNetPay());
            stmt.setInt(5, payroll.getPayPeriodID());
            stmt.setInt(6, payroll.getEmployeeID());
            stmt.setInt(7, payroll.getPayrollID());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deletePayroll(int payrollID) throws SQLException {
        String query = "DELETE FROM payroll WHERE payrollID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payrollID);
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Payroll POJO
    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException {
        Payroll payroll = new Payroll();
        payroll.setPayrollID(rs.getInt("payrollID"));
        payroll.setGrossPay(rs.getDouble("grossPay"));
        payroll.setTotalDeductions(rs.getDouble("totalDeductions"));
        payroll.setWithholdingTax(rs.getDouble("withholdingTax"));
        payroll.setNetPay(rs.getDouble("netPay"));
        payroll.setPayPeriodID(rs.getInt("payPeriodID"));
        payroll.setEmployeeID(rs.getInt("employeeID"));
        return payroll;
    }
}