package org.example;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IMS {

    private final String DB_URL = "jdbc:mysql://localhost:3306/inventory";  // The URL of the database the IMS is working from
    private final String Username = "root";                                 // Username for DB
    private final String Password = "root";                                 // Password for DB
    private Statement sqlSt;                                                // Statement used to execute sql commands using the database.
    private Connection dbConnect;                                           // The bridge between the database and this java file.
    private final Scanner consoleReader = new Scanner(System.in);
    private User currentUser;
    private Inventory inventory;

    public IMS() {
        attemptDBConnection();
        inventory = new Inventory(sqlSt);
    }

    public User getCurrentUser() {
        return currentUser;
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

        for(int i = 0; i < 5; i++) {
            System.out.print("Enter your username: ");
            String unvalidatedUserName = consoleReader.nextLine().strip().toLowerCase();
            System.out.print("Enter your password: ");
            String unvalidatedPassword = consoleReader.nextLine().strip();
            if(checkIfValidUser(unvalidatedUserName, unvalidatedPassword))
                return true;
        }

        System.out.println("Too many attempts! Closing...");
        return false;
    }

    public int promptUser() {
        System.out.println("\nWelcome, "+getCurrentUser().getUpperName()+"!");
        System.out.println("1. View Products\n2. Search Products\n3. Record Sale\n4. Exit");
        while(true) {
            System.out.print("> ");
            int response;
            try {
                response = Integer.parseInt(consoleReader.nextLine().strip());
                if(response > 0 && response < 5) return response;
                else System.out.println("Invalid Choice. Enter 1, 2, 3, 4");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a number.");
            }
        }
    }

    public int promptAdmin() {
        return 0;
    }

    public void printInventory() {
        if(inventory.isEmpty()) {
            System.out.println("There are no products in inventory.");
            return;
        }
        System.out.println();
        inventory.displayInventory();
    }

    public void searchInventory() {
        System.out.println("\nEnter the name of the product.");
        while(true) {
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            Product prod = inventory.searchForProduct(response);
            if(prod == null) {
                System.out.println("Product with name \""+response+"\" was not found.");
            }
            else {
                System.out.format("%nItem: %s Quantity: %d Price: %.2f%n", prod.getItemName(), prod.getQuantity(), prod.getPrice());
                return;
            }
        }
    }

    public static void main(String[] args)
    {
        IMS inventorySystem = new IMS();
        if(!(inventorySystem.authenticateUser())) System.exit(0);
        if(inventorySystem.getCurrentUser().getRole().equals("user")) {
            while (true) {
                int choice = inventorySystem.promptUser();
                switch (choice) {
                    case 1:
                        inventorySystem.printInventory();
                        break;
                    case 2:
                        inventorySystem.searchInventory();
                        break;
                    case 3:
                        break;
                    case 4:
                        System.out.println("Closing ...");
                        System.exit(0);
                        break;
                    default:
                        break;

                }
            }
        }
        else {
            while (true) {
                int choice = inventorySystem.promptAdmin();
            }
        }
    }
}