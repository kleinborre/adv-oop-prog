package dao;

import pojo.Payroll;

import java.sql.SQLException;
import java.util.List;

public interface PayrollDAO {

    Payroll getPayrollByID(int payrollID) throws SQLException;

    List<Payroll> getPayrollsByEmployeeID(int employeeID) throws SQLException;

    List<Payroll> getAllPayrolls() throws SQLException;
    
    List<Payroll> getAllPayrollsByPeriod(int payPeriodID) throws SQLException;

    void addPayroll(Payroll payroll) throws SQLException;

    void updatePayroll(Payroll payroll) throws SQLException;

    void deletePayroll(int payrollID) throws SQLException;
}