package util;

public class SessionManager {

    private static String userID;
    private static int employeeID;

    public static void setSession(String userID, int employeeID) {
        SessionManager.userID = userID;
        SessionManager.employeeID = employeeID;
    }

    public static String getUserID() {
        return userID;
    }

    public static int getEmployeeID() {
        return employeeID;
    }

    public static void clearSession() {
        userID = null;
        employeeID = -1;
    }
}