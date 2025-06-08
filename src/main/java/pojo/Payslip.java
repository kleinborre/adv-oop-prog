package pojo;

public class Payslip {

    private int employeeID;
    private String employeeName;
    private int payPeriodID;
    private String payPeriodName;
    private double grossPay;
    private double totalDeductions;
    private double withholdingTax;
    private double netPay;
    private double totalHoursWorked;
    private double hourPayAmount;
    private double totalLeaveDays;
    private double overtimeHours;
    private double overtimePay;

    // Constructors

    public Payslip() {
    }

    public Payslip(int employeeID, String employeeName, int payPeriodID, String payPeriodName, double grossPay,
                   double totalDeductions, double withholdingTax, double netPay, double totalHoursWorked,
                   double hourPayAmount, double totalLeaveDays, double overtimeHours, double overtimePay) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.payPeriodID = payPeriodID;
        this.payPeriodName = payPeriodName;
        this.grossPay = grossPay;
        this.totalDeductions = totalDeductions;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
        this.totalHoursWorked = totalHoursWorked;
        this.hourPayAmount = hourPayAmount;
        this.totalLeaveDays = totalLeaveDays;
        this.overtimeHours = overtimeHours;
        this.overtimePay = overtimePay;
    }

    // Getters and Setters

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getPayPeriodID() {
        return payPeriodID;
    }

    public void setPayPeriodID(int payPeriodID) {
        this.payPeriodID = payPeriodID;
    }

    public String getPayPeriodName() {
        return payPeriodName;
    }

    public void setPayPeriodName(String payPeriodName) {
        this.payPeriodName = payPeriodName;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(double grossPay) {
        this.grossPay = grossPay;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(double withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double netPay) {
        this.netPay = netPay;
    }

    public double getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(double totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }

    public double getHourPayAmount() {
        return hourPayAmount;
    }

    public void setHourPayAmount(double hourPayAmount) {
        this.hourPayAmount = hourPayAmount;
    }

    public double getTotalLeaveDays() {
        return totalLeaveDays;
    }

    public void setTotalLeaveDays(double totalLeaveDays) {
        this.totalLeaveDays = totalLeaveDays;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(double overtimePay) {
        this.overtimePay = overtimePay;
    }
}