package service;

import daoimpl.PayslipDAOImpl;
import daoimpl.PayrollDAOImpl;
import daoimpl.SystemManagementDAOImpl;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import pojo.Payslip;
import pojo.Payroll;
import pojo.SystemManagement;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private PayslipDAOImpl payslipDAO;
    private PayrollDAOImpl payrollDAO;
    private SystemManagementDAOImpl systemManagementDAO;

    private static final String OUTPUT_DIR = "generated_reports";

    public ReportService() {
        try {
            payslipDAO = new PayslipDAOImpl();
            payrollDAO = new PayrollDAOImpl();
            systemManagementDAO = new SystemManagementDAOImpl();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing DAOs in ReportService", e);
        }

        // Ensure output directory exists
        createOutputDirectory();
    }

    private void createOutputDirectory() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("Warning: Could not create output directory: " + OUTPUT_DIR);
            }
        }
    }

    public void generatePayslipReport(int employeeID, int payPeriodID) {
        try {
            Payslip payslip = payslipDAO.getPayslipByEmployeeAndPeriod(employeeID, payPeriodID);

            if (payslip == null) {
                throw new RuntimeException("No Payslip found for employeeID=" + employeeID + " payPeriodID=" + payPeriodID);
            }

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of(payslip));

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Employee Payslip Report");

            JasperReport jasperReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/reports/PayslipReport.jrxml")
            );
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            String outputPath = OUTPUT_DIR + "/PayslipReport_" + employeeID + "_" + payPeriodID + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("Payslip Report generated successfully: " + outputPath);

        } catch (Exception e) {
            throw new RuntimeException("Error generating Payslip Report", e);
        }
    }

    public void generatePayrollSummaryReport(int payPeriodID) {
        try {
            List<Payroll> payrollList = payrollDAO.getAllPayrollsByPeriod(payPeriodID);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(payrollList);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Payroll Summary Report");

            JasperReport jasperReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/reports/PayrollSummaryReport.jrxml")
            );
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            String outputPath = OUTPUT_DIR + "/PayrollSummaryReport_" + payPeriodID + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("Payroll Summary Report generated successfully: " + outputPath);

        } catch (Exception e) {
            throw new RuntimeException("Error generating Payroll Summary Report", e);
        }
    }

    public void generateSessionManagementReport(java.sql.Date fromDate, java.sql.Date toDate) {
        try {
            List<SystemManagement> systemLogs = systemManagementDAO.getAllSystemLogs();

            // Optionally filter logs here by date (if not done in DAO already)

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(systemLogs);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Session Management Report");
            parameters.put("FromDate", fromDate);
            parameters.put("ToDate", toDate);

            JasperReport jasperReport = JasperCompileManager.compileReport(
                    getClass().getResourceAsStream("/reports/SessionManagementReport.jrxml")
            );
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            String outputPath = OUTPUT_DIR + "/SessionManagementReport_" + fromDate + "_to_" + toDate + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

            System.out.println("Session Management Report generated successfully: " + outputPath);

        } catch (Exception e) {
            throw new RuntimeException("Error generating Session Management Report", e);
        }
    }
}