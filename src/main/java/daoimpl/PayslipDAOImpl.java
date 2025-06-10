package daoimpl;

import dao.PayslipDAO;
import db.DatabaseConnection;
import pojo.Payslip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayslipDAOImpl implements PayslipDAO {

    private Connection connection;

    public PayslipDAOImpl() throws SQLException {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Payslip getPayslipByEmployeeAndPeriod(int employeeID, int payPeriodID) throws SQLException {
        String query = getPayslipBaseQuery() +
                " WHERE p.employeeID = ? AND p.payPeriodID = ? " +
                getPayslipGroupByClause();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);
            stmt.setInt(2, payPeriodID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayslip(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Payslip> getPayslipsByEmployeeID(int employeeID) throws SQLException {
        List<Payslip> payslipList = new ArrayList<>();
        String query = getPayslipBaseQuery() +
                " WHERE p.employeeID = ? " +
                getPayslipGroupByClause();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payslipList.add(mapResultSetToPayslip(rs));
                }
            }
        }
        return payslipList;
    }

    @Override
    public List<Payslip> getAllPayslipsByPeriod(int payPeriodID) throws SQLException {
        List<Payslip> payslipList = new ArrayList<>();
        String query = getPayslipBaseQuery() +
                " WHERE p.payPeriodID = ? " +
                getPayslipGroupByClause();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payPeriodID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payslipList.add(mapResultSetToPayslip(rs));
                }
            }
        }
        return payslipList;
    }

    @Override
    public List<Payslip> getAllPayslips() throws SQLException {
        List<Payslip> payslipList = new ArrayList<>();
        String query = getPayslipBaseQuery() + getPayslipGroupByClause();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                payslipList.add(mapResultSetToPayslip(rs));
            }
        }
        return payslipList;
    }

    // Helper method to generate base SQL query
    private String getPayslipBaseQuery() {
        return "SELECT " +
                "p.employeeID, " +
                "CONCAT(e.firstName, ' ', e.lastName) AS employeeName, " +
                "p.payPeriodID, " +
                "pp.periodName AS payPeriodName, " +
                "p.grossPay, p.totalDeductions, p.withholdingTax, p.netPay, " +
                "ph.totalHours, ph.amountPay AS hourPayAmount, " +
                "COALESCE(SUM(pl.leaveDays), 0) AS totalLeaveDays, " +
                "COALESCE(SUM(po.overtimeHours), 0) AS overtimeHours, " +
                "COALESCE(SUM(po.overtimePay), 0) AS overtimePay " +
                "FROM payroll p " +
                "JOIN employee e ON p.employeeID = e.employeeID " +
                "JOIN payperiod pp ON p.payPeriodID = pp.payPeriodID " +
                "LEFT JOIN payrollHourPay ph ON p.payrollID = ph.payrollID " +
                "LEFT JOIN payrollLeave pl ON p.payrollID = pl.payrollID " +
                "LEFT JOIN payrollOvertime po ON p.payrollID = po.payrollID ";
    }

    // Helper method to generate GROUP BY clause (DRY!)
    private String getPayslipGroupByClause() {
        return " GROUP BY " +
                "p.payrollID, p.employeeID, e.firstName, e.lastName, " +
                "p.payPeriodID, pp.periodName, " +
                "p.grossPay, p.totalDeductions, p.withholdingTax, p.netPay, " +
                "ph.totalHours, ph.amountPay";
    }

    // Helper method to map ResultSet to Payslip POJO
    private Payslip mapResultSetToPayslip(ResultSet rs) throws SQLException {
        Payslip payslip = new Payslip();
        payslip.setEmployeeID(rs.getInt("employeeID"));
        payslip.setEmployeeName(rs.getString("employeeName"));
        payslip.setPayPeriodID(rs.getInt("payPeriodID"));
        payslip.setPayPeriodName(rs.getString("payPeriodName"));
        payslip.setGrossPay(rs.getDouble("grossPay"));
        payslip.setTotalDeductions(rs.getDouble("totalDeductions"));
        payslip.setWithholdingTax(rs.getDouble("withholdingTax"));
        payslip.setNetPay(rs.getDouble("netPay"));
        payslip.setTotalHoursWorked(rs.getDouble("totalHours"));
        payslip.setHourPayAmount(rs.getDouble("hourPayAmount"));
        payslip.setTotalLeaveDays(rs.getDouble("totalLeaveDays"));
        payslip.setOvertimeHours(rs.getDouble("overtimeHours"));
        payslip.setOvertimePay(rs.getDouble("overtimePay"));
        return payslip;
    }
}