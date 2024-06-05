package org.example;
import java.sql.*;
import java.util.Date;
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
            System.out.println("\nAttempting to log in...");
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
            System.out.print("\nEnter your username: ");
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
        if(inventory.isEmpty()) {
            System.out.println("There are no products in inventory.");
            return;
        }
        System.out.println("\nEnter the name of the product.\nEnter 'exit' to return to the menu.");
        while(true) {
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return;
            Product prod = inventory.searchForProductByName(response);
            if(prod == null) {
                System.out.println("Product with name \""+response+"\" was not found. Try again.");
            }
            else {
                System.out.format("%nItem: %s Quantity: %d Price: %.2f%n", prod.getItemName(), prod.getQuantity(), prod.getPrice());
                return;
            }
        }
    }

    public Product searchForProduct(int id) {
        if(id < 1) return null;
        return inventory.searchForProductByID(id);
    }

    public Product gatherProductToSell() {
        Product toBeSold;
        while(true) {
            System.out.print("> ");
            int id;
            try {
                String response = consoleReader.nextLine().strip().toLowerCase();
                if(response.equals("exit")) return null;
                id = Integer.parseInt(response);
                toBeSold = searchForProduct(id);
                if(toBeSold == null) {
                    System.out.println("Product with that ID does not exist. Try again.");
                    continue;
                }
                return toBeSold;

            } catch(NumberFormatException ex) {
                System.out.println("Please enter a number for the ID of the product.");
            }
        }
    }

    public int gatherQuantityToSell(Product toBeSold) {
        int quantityToBeSold;
        while(true) {
            System.out.print("> ");
            try {
                String response = consoleReader.nextLine().strip().toLowerCase();
                if(response.equals("exit")) return 0;
                quantityToBeSold = Integer.parseInt(response);
                if(quantityToBeSold < 1) {
                    System.out.println("Invalid amount to sell. Value must be greater than 0. Try again.");
                    continue;
                }
                else if(quantityToBeSold > toBeSold.getQuantity()) {
                    System.out.println("Value given is greater than the amount in inventory. Try again.");
                    continue;
                }
                return quantityToBeSold;
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a number for the quantity of product to sell.");
            }
        }
    }

    public void sellProduct() {

        try {
            dbConnect.setAutoCommit(false);

            System.out.println("\nEnter Product ID to sell.\nEnter 'exit' to return to the menu.");
            Product toSell = gatherProductToSell();
            if(toSell == null) return;


            System.out.println("\nEnter quantity to sell.\nEnter 'exit' to return to the menu.");
            int quantity = gatherQuantityToSell(toSell);
            if(quantity == 0) return;

            System.out.println("\nUpdating Inventory...");
            String sellSQL = "update inventory set Quantity = Quantity - ? where InventoryID = ?;";
            PreparedStatement updateInventory = dbConnect.prepareStatement(sellSQL);
            updateInventory.setInt(1, quantity);
            updateInventory.setInt(2, toSell.getInventoryID());
            updateInventory.executeUpdate();

            // Date date = new Date();
            System.out.println("Creating a receipt...");
            Object timeParam = new java.sql.Timestamp(new Date().getTime());
            String createSaleSQL = "insert into sales (InventoryID, Quantity, SaleDate) values (?, ?, ?);";
            PreparedStatement updateSales = dbConnect.prepareStatement(createSaleSQL);
            updateSales.setInt(1, toSell.getInventoryID());
            updateSales.setInt(2, quantity);
            updateSales.setObject(3, timeParam);
            updateSales.executeUpdate();

            dbConnect.commit();

            System.out.println("Sale was made!");

            dbConnect.setAutoCommit(true);
            inventory.initializeInventory();

        } catch (SQLException ex) {
            System.out.println("Product Sale Failed: "+ ex.getMessage());
            try {
                if(dbConnect != null) {
                    dbConnect.rollback();
                    System.out.println("Sale was rolled back due to error.");
                }
            } catch (SQLException rollEx) {
                System.out.println("Rollback Failed: "+rollEx.getMessage());
            }
        }
    }

    public void close() {
        try {
            sqlSt.close();
            dbConnect.close();
            consoleReader.close();
        } catch(SQLException ex) {
            System.out.println("Normal Close Failed: "+ex.getMessage());
        }
        System.exit(0);
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
                        inventorySystem.sellProduct();
                        break;
                    case 4:
                        System.out.println("Closing ...");
                        inventorySystem.close();
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