package service;

import dao.PayrollDAO;
import daoimpl.PayrollDAOImpl;
import pojo.Payroll;

import java.sql.SQLException;
import java.util.List;

public class PayrollService {

    private PayrollDAO payrollDAO;

    public PayrollService() {
        try {
            payrollDAO = new PayrollDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing PayrollDAO", e);
        }
    }

    public Payroll getPayrollByID(int payrollID) {
        try {
            return payrollDAO.getPayrollByID(payrollID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payroll by ID", e);
        }
    }

    public List<Payroll> getPayrollsByEmployeeID(int employeeID) {
        try {
            return payrollDAO.getPayrollsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payrolls by employee ID", e);
        }
    }

    public List<Payroll> getAllPayrolls() {
        try {
            return payrollDAO.getAllPayrolls();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all payrolls", e);
        }
    }

    public void addPayroll(Payroll payroll) {
        try {
            payrollDAO.addPayroll(payroll);
        } catch (SQLException e) {
            throw new RuntimeException("Error adding payroll", e);
        }
    }

    public void updatePayroll(Payroll payroll) {
        try {
            payrollDAO.updatePayroll(payroll);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payroll", e);
        }
    }

    public void deletePayroll(int payrollID) {
        try {
            payrollDAO.deletePayroll(payrollID);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting payroll", e);
        }
    }
}