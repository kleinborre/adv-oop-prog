package service;

import dao.PayslipDAO;
import daoimpl.PayslipDAOImpl;
import pojo.Payslip;

import java.sql.SQLException;
import java.util.List;

public class PayslipService {

    private PayslipDAO payslipDAO;

    public PayslipService() {
        try {
            payslipDAO = new PayslipDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing PayslipDAO", e);
        }
    }

    public Payslip getPayslipByEmployeeAndPeriod(int employeeID, int payPeriodID) {
        try {
            return payslipDAO.getPayslipByEmployeeAndPeriod(employeeID, payPeriodID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payslip by employee and period", e);
        }
    }

    public List<Payslip> getPayslipsByEmployeeID(int employeeID) {
        try {
            return payslipDAO.getPayslipsByEmployeeID(employeeID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving payslips by employee ID", e);
        }
    }

    public List<Payslip> getAllPayslipsByPeriod(int payPeriodID) {
        try {
            return payslipDAO.getAllPayslipsByPeriod(payPeriodID);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all payslips by period", e);
        }
    }

    public List<Payslip> getAllPayslips() {
        try {
            return payslipDAO.getAllPayslips();
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all payslips", e);
        }
    }
}