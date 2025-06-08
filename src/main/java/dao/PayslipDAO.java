package dao;

import pojo.Payslip;

import java.sql.SQLException;
import java.util.List;

public interface PayslipDAO {

    Payslip getPayslipByEmployeeAndPeriod(int employeeID, int payPeriodID) throws SQLException;

    List<Payslip> getPayslipsByEmployeeID(int employeeID) throws SQLException;

    List<Payslip> getAllPayslipsByPeriod(int payPeriodID) throws SQLException;

    List<Payslip> getAllPayslips() throws SQLException;
}