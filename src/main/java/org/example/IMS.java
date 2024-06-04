package org.example;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IMS {

    private final String DB_URL = "jdbc:mysql://localhost:3306/inventory"; // The URL of the database the IMS is working from
    private final String Username = "root";                                 // Username for DB
    private final String Password = "root";                                 // Password for DB
    private Statement sqlSt;                                                // Statement used to execute sql commands using the database.
    private Connection dbConnect;                                           // The bridge between the database and this java file.
    private final Scanner consoleReader = new Scanner(System.in);
    private User currentUser;

    public IMS() {
        attemptDBConnection();
    }

    public void attemptDBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnect = DriverManager.getConnection(DB_URL, Username, Password);
            sqlSt = dbConnect.createStatement();    // Allows SQL to be executed
            System.out.println("Connection with database successful!");

        }
        catch(ClassNotFoundException ex) {
            Logger.getLogger(IMS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Class not found, check the JAR");
            System.exit(0);
        }
        catch(SQLException ex) {
            Logger.getLogger(IMS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL is bad! " + ex.getMessage());
            System.exit(0);
        }
    }

    public boolean checkIfValidUser(String usName, String usPass) {
        String checkUserSQL = "Select * from users where Username='"+usName+"' and Password='"+usPass+"';";
        ResultSet result;
        try {
            result = sqlSt.executeQuery(checkUserSQL);
            System.out.println("Attempting to log in...");
            if(!result.isBeforeFirst()) {
                System.out.println("No user was found with that username and password.");
                return false;
            }
            System.out.println("User found! You're logged in.");
            result.next();
            currentUser = new User(result.getInt("UserID"), result.getString("Name"),
                    result.getString("Username"), result.getString("Password"),
                    result.getString("Role"));
            return true;
        } catch(SQLException ex) {
            System.out.println("User Validation Failed: "+ex.getMessage());
            return false;
        }

    }

    public boolean authenticateUser() {
        while(true) {
            System.out.print("Enter your username: ");
            String unvalidatedUserName = consoleReader.nextLine().strip().toLowerCase();
            System.out.print("Enter your password: ");
            String unvalidatedPassword = consoleReader.nextLine().strip();
            if(checkIfValidUser(unvalidatedUserName, unvalidatedPassword))
                return true;
        }
    }

    public static void main(String[] args)
    {
        IMS inventorySystem = new IMS();
        inventorySystem.authenticateUser();
    }
}