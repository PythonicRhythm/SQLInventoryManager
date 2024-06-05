package org.example;
import java.sql.*;
import java.util.ArrayList;
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
        System.out.println("\nWelcome, "+getCurrentUser().getUpperName()+" (admin)!");
        System.out.println("1. View Products\n2. Search Products\n3. Record Sale\n4. Add Product" +
                "\n5. Delete Product\n6. Update Product\n7. Generate Inventory Report\n8. Generate Sales Report" +
                "\n9. Exit");
        while(true) {
            System.out.print("> ");
            int response;
            try {
                response = Integer.parseInt(consoleReader.nextLine().strip());
                if(response > 0 && response < 10) return response;
                else System.out.println("Invalid Choice. Enter 1 -> 9");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a number.");
            }
        }
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
                if(toBeSold.getQuantity() == 0) {
                    System.out.println("We are currently out of stock of product \""+toBeSold.getItemName()+"\". Sorry!");
                    return null;
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

    public boolean confirmConsentOfPurchase(int quantity, String name) {
        // Confirmation before sale is made
        System.out.format("\nAre you sure you want to buy %d of \"%s\"? (Y/N)%n", quantity, name);
        while(true) {
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("y")) return true;
            else if(response.equals("n")) return false;
            else {
                System.out.println("Invalid Input. Enter either Y/N.");
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

            if(!confirmConsentOfPurchase(quantity, toSell.getItemName()))
                return;

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

    public String gatherNewItemName() {
        while (true) {
            System.out.print("ItemName: ");
            String name = consoleReader.nextLine().strip().toLowerCase();
            if(name.equals("exit")) return null;
            if(name.length() > 254) {
                System.out.println("ItemName is too long. Has to be less than 254 characters.");
                continue;
            }
            else if(name.length() < 5) {
                System.out.println("ItemName is expected to be greater than 4 characters. Try again.");
                continue;
            }
            else if(inventory.hasProductWithName(name)) {
                System.out.println("There is a product with that name already. Must have unique names.");
                continue;
            }

            return name;
        }
    }

    public int gatherNewItemQuantity() {
        while (true) {
            System.out.print("Quantity: ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return -1;

            int value;
            try {
                value = Integer.parseInt(response);
                if(value < 0) {
                    System.out.println("Can not have negative quantity!");
                    continue;
                }
            } catch(NumberFormatException ex) {
                System.out.println("Please enter a number for the quantity.");
                continue;
            }

            return value;
        }
    }

    public double gatherNewItemPrice() {
        while (true) {
            System.out.print("Price: ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return -1;

            double value;
            try {
                value = Double.parseDouble(response);
                if(value < 0) {
                    System.out.println("Can not have negative price!");
                    continue;
                }
            } catch(NumberFormatException ex) {
                System.out.println("Please enter a number for the price.");
                continue;
            }

            return value;
        }
    }

    public void addProduct() {
        System.out.println("\nExpecting an ItemName, Quantity, Price values.\nEnter 'exit' to return to menu.");
        String ItemName = gatherNewItemName();
        if(ItemName == null) return;

        int quantity = gatherNewItemQuantity();
        if(quantity == -1) return;

        double price = gatherNewItemPrice();
        if(price < 0) return;

        System.out.println("\nAttempting to add new item \""+ItemName+"\"...");
        String addSQL = "insert into inventory (ItemName, Quantity, Price) values (?, ?, ?);";
        try{

            PreparedStatement addNewItem = dbConnect.prepareStatement(addSQL);
            addNewItem.setString(1, ItemName.substring(0,1).toUpperCase() + ItemName.substring(1));
            addNewItem.setInt(2, quantity);
            addNewItem.setDouble(3, price);
            addNewItem.executeUpdate();

            System.out.println("Addition of item \""+ItemName+"\" was successful!");
            inventory.initializeInventory();

        } catch(SQLException ex) {
            System.out.println("Add New Item Failure: "+ex.getMessage());
        }

    }

    public void deleteProduct() {
        System.out.println("\nWhat is the ID of the item chosen for deletion?\nEnter 'exit' to return to menu.");
        Product toBeDeleted;
        while(true) {
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return;

            try {
                int ID = Integer.parseInt(response);
                if(ID < 1) {
                    System.out.println("ID cannot be less than 1. Try again.");
                }

                Product possibleNull = inventory.searchForProductByID(ID);
                if(possibleNull == null) {
                    System.out.println("No product exists with that ID. Try again.");
                    continue;
                }

                toBeDeleted = possibleNull;
                break;

            } catch(NumberFormatException ex) {
                System.out.println("Please enter a number for the ID.");
            }
        }

        System.out.println("\nAre you sure you want to delete the \""+toBeDeleted.getItemName()+"\" entry? (Y/N)");
        while(true){
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("y")) break;
            else if(response.equals("n")) return;
            else {
                System.out.println("Please enter 'Y' or 'N'.");
            }
        }

        System.out.println("\nAttempting to delete item \""+toBeDeleted.getItemName()+"\"...");
        String deleteSQL = "delete from inventory where InventoryID = ?;";
        try{

            PreparedStatement deleteItem = dbConnect.prepareStatement(deleteSQL);
            deleteItem.setInt(1, toBeDeleted.getInventoryID());
            deleteItem.executeUpdate();

            System.out.println("Deletion of item \""+toBeDeleted.getItemName()+"\" was successful!");
            inventory.initializeInventory();

        } catch(SQLException ex) {
            System.out.println("Add New Item Failure: "+ex.getMessage());
        }

    }

//    public void generateSalesReport() {
//
//    }

    public void close() {
        try {
            sqlSt.close();
            dbConnect.close();
            consoleReader.close();
        } catch(SQLException ex) {
            System.out.println("Normal Close Failed: "+ex.getMessage());
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
                        inventorySystem.sellProduct();
                        break;
                    case 4:
                        System.out.println("Closing ...");
                        inventorySystem.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Response Error: choice given is outside of bounds.");
                        break;

                }
            }
        }
        else {
            while (true) {
                int choice = inventorySystem.promptAdmin();
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
                        inventorySystem.addProduct();
                        break;
                    case 5:
                        inventorySystem.deleteProduct();
                        break;
                    case 6:
                        System.out.println("Not Implemented Yet");
                        //inventorySystem.updateProduct();
                        break;
                    case 7:
                        System.out.println("Not Implemented Yet");
                        //inventorySystem.generateInventoryReport();
                        break;
                    case 8:
                        System.out.println("Not Implemented Yet");
                        //inventorySystem.generateSalesReport();
                        break;
                    case 9:
                        System.out.println("Closing...");
                        inventorySystem.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Response Error: choice given is outside of bounds.");
                        break;
                }
            }
        }
    }
}