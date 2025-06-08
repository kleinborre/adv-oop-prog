package pojo;

public class Payroll {

    private int payrollID;
    private double grossPay;
    private double totalDeductions;
    private double withholdingTax;
    private double netPay;
    private int payPeriodID;
    private int employeeID;

    // Constructors

    public Payroll() {
    }

    public Payroll(int payrollID, double grossPay, double totalDeductions, double withholdingTax, double netPay,
                   int payPeriodID, int employeeID) {
        this.payrollID = payrollID;
        this.grossPay = grossPay;
        this.totalDeductions = totalDeductions;
        this.withholdingTax = withholdingTax;
        this.netPay = netPay;
        this.payPeriodID = payPeriodID;
        this.employeeID = employeeID;
    }

    // Getters and Setters

    public int getPayrollID() {
        return payrollID;
    }

    public void setPayrollID(int payrollID) {
        this.payrollID = payrollID;
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

    public int getPayPeriodID() {
        return payPeriodID;
    }

    public void setPayPeriodID(int payPeriodID) {
        this.payPeriodID = payPeriodID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }
}