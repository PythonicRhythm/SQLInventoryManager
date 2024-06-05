package org.example;
import java.sql.*;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
    The IMS class represents a vendor's inventory system with two separate role functionalities
    based on if the user who is logged in is a regular user or an admin. A regular user has the
    ability to view the products currently in inventory, search for individual products, and
    create a sale record. The admin user has all the functionality of a regular user plus the
    ability to add, delete, update products, generate sales reports, and generate inventory reports.
    The IMS class interacts with a locally hosted MySQL database that tracks inventory, sales, and user
    tables via the Inventory and SalesReport classes.
 */

public class IMS {

    private final String DB_URL = "jdbc:mysql://localhost:3306/inventory";  // The URL of the database the IMS is working from
    private final String Username = "root";                                 // Username for DB
    private final String Password = "root";                                 // Password for DB
    private Statement sqlSt;                                                // Statement used to execute sql commands using the database.
    private Connection dbConnect;                                           // The bridge between the database and this java file.
    private final Scanner consoleReader = new Scanner(System.in);           // Console Input Reader
    private User currentUser;                                               // The active user
    private final Inventory inventory;                                      // The Inventory obj which tracks all inventory products
    private final SalesReport salesReport;                                  // The SalesReport obj which tracks all sale records.

    // Initialize connection with MySQL
    // and gather inventory and sales status
    public IMS() {
        attemptDBConnection();
        inventory = new Inventory(sqlSt);
        salesReport = new SalesReport(sqlSt);
    }

    // Retrieve the active user.
    public User getCurrentUser() {
        return currentUser;
    }

    // attemptDBConnection() will attempt to connect
    // with the locally hosted MySQL database and present
    // the user with any errors that may occur throughout
    // the connection.
    public void attemptDBConnection() {
        try {
            // Attempt to connect with MySQL database.
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnect = DriverManager.getConnection(DB_URL, Username, Password);
            sqlSt = dbConnect.createStatement();    // Allows SQL to be executed
            System.out.println("Connection with database successful!");

        }
        catch(ClassNotFoundException ex) {
            // Failure with JAR
            Logger.getLogger(IMS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Class not found, check the JAR");
            System.exit(0);
        }
        catch(SQLException ex) {
            // Failure with MySQL
            Logger.getLogger(IMS.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL is bad! " + ex.getMessage());
            System.exit(0);
        }
    }

    // checkIfValidUser() will attempt to find a user with the
    // credentials provided to the function. If the user was not
    // found, the user will be informed. If the user was found,
    // the active user is set and the user is informed.
    public boolean checkIfValidUser(String usName, String usPass) {

        // SQL code for grabbing any users that match given username and password.
        String checkUserSQL = "Select * from users where Username='"+usName+"' and Password='"+usPass+"';";
        ResultSet result;
        try {
            // Gather database response.
            result = sqlSt.executeQuery(checkUserSQL);
            System.out.println("\nAttempting to log in...");

            // If response is empty, no user found.
            if(!result.isBeforeFirst()) {
                System.out.println("No user was found with that username and password.");
                return false;
            }

            // else user found, proceed and assign active user.
            System.out.println("User found! You're logged in.");
            result.next();
            currentUser = new User(result.getString("Name"), result.getString("Role"));
            return true;
        } catch(SQLException ex) {
            System.out.println("User Validation Failed: "+ex.getMessage());
            return false;
        }

    }

    // authenticateUser() will prompt the user for a username
    // and password to send to checkIfValidUser() to confirm
    // if the user exists. If a failure occurs the user will
    // be prompted again. If the user exceeds 5 attempts, the
    // program will end.
    public boolean authenticateUser() {

        // User has 5 attempts to correctly sign in or program shuts down.
        for(int i = 0; i < 5; i++) {

            // Gather username and password.
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

    // promptUser() will prompt the user with the choices a user can make
    // such as View Products, Search Products, Record Sale, and Exit.
    // The while loop will continue to loop unless the user exits or
    // a proper choice has been made.
    public int promptUser() {

        // Print choices user can take.
        System.out.println("\nWelcome, "+getCurrentUser().getUpperName()+"!");
        System.out.println("1. View Products\n2. Search Products\n3. Record Sale\n4. Exit (or type 'exit')");
        while(true) {
            System.out.print("> ");
            int response;
            try {
                // Gather user response.
                String choice = consoleReader.nextLine().strip().toLowerCase();
                if(choice.equals("exit")) {
                    System.out.println("Closing...");
                    close();
                    System.exit(0);
                }
                response = Integer.parseInt(choice);
                // If valid choice, return it.
                if(response > 0 && response < 5) return response;
                // else, inform user of mistake.
                else System.out.println("Invalid Choice. Enter 1, 2, 3, 4");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a number that matches an option on the list.");
            }
        }
    }

    // promptAdmin() will prompt the admin with the choices an admin can make
    // such as View Products, Search Products, Record Sale, Add Product,
    // Delete Product, Update Product, Generate Inventory Report, Generate
    // Sales Report, and Exit.The while loop will continue to loop unless the
    // admin exits or a proper choice has been made.
    public int promptAdmin() {

        // Print choices admin can take.
        System.out.println("\nWelcome, "+getCurrentUser().getUpperName()+" (admin)!");
        System.out.println("1. View Products\n2. Search Products\n3. Record Sale\n4. Add Product" +
                "\n5. Delete Product\n6. Update Product\n7. Generate Inventory Report\n8. Generate Sales Report" +
                "\n9. Exit (or type 'exit')");

        while(true) {
            System.out.print("> ");
            int response;
            try {
                // Gather admin response.
                String choice = consoleReader.nextLine().strip().toLowerCase();
                if(choice.equals("exit")) {
                    System.out.println("Closing...");
                    close();
                    System.exit(0);
                }

                response = Integer.parseInt(choice);
                // If valid choice, return it.
                if(response > 0 && response < 10) return response;
                // else, inform admin of mistake.
                else System.out.println("Invalid Choice. Enter 1 -> 9");

            } catch (NumberFormatException ex) {
                // Admin typed a string, inform them of proper procedure.
                System.out.println("Enter a number that matches an option on the list..");
            }
        }
    }

    // printInventory() will print all the current products in the inventory.
    // If no products currently stored, informs the user/admin. Prints inventory
    // by calling the Inventory.displayInventory() method.
    public void printInventory() {

        // If inventory contain no products, inform user/admin and do not proceed.
        if(inventory.isEmpty()) {
            System.out.println("There are no products in inventory.");
            return;
        }

        // else gather all products and display them.
        System.out.println();
        inventory.displayInventory();
    }

    // generateSalesReport() will print all the sales that have occurred
    // involving the inventory's products. If no sales have been recorded,
    // informs the user/admin. Prints the sales by calling SalesReport.displaySales().
    public void generateSalesReport() {

        // If no sales records exist, let admin know.
        if(salesReport.isEmpty()) {
            System.out.println("No sales have been recorded.");
            return;
        }

        // else gather all sales and display.
        System.out.println();
        salesReport.displaySales();
    }

    // searchInventory() will search the inventory for a product
    // using a given name by the user/admin. If the product wasn't
    // found, the user/admin is informed. If the product is found,
    // it will display information about the product.
    public void searchInventory() {

        // If inventory is empty, do not proceed with search, inform user/admin.
        if(inventory.isEmpty()) {
            System.out.println("There are no products in inventory.");
            return;
        }
        System.out.println("\nEnter the name of the product.\nEnter 'exit' to return to the menu.");
        while(true) {
            // Gather response from user/admin.
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return;
            Product prod = inventory.searchForProductByName(response);

            // If prod is null, product does not exist, inform the user/admin.
            if(prod == null) {
                System.out.println("Product with name \""+response+"\" was not found. Try again.");
            }
            else {
                // Product is found, provide details of item.
                System.out.format("%nItem: %s Quantity: %d Price: %.2f%n", prod.getItemName(), prod.getQuantity(), prod.getPrice());
                return;
            }
        }
    }

    // searchForProduct() will search the inventory for a product
    // using a given ID by the user/admin. If the product wasn't
    // found, return null. If the product was found, return the product.
    public Product searchForProduct(int id) {
        if(id < 1) return null; // No negative ids.
        return inventory.searchForProductByID(id);
    }

    // gatherProductToSell() will gather the specific product the user/admin
    // wants to purchase and return it if found. Informs the user if the
    // product is out of stock.
    public Product gatherProductToSell() {
        Product toBeSold;
        while(true) {
            System.out.print("> ");
            int id;
            try {
                // Gather response from user/admin.
                String response = consoleReader.nextLine().strip().toLowerCase();
                if(response.equals("exit")) return null;
                id = Integer.parseInt(response);
                toBeSold = searchForProduct(id);

                // If product returned is null, the product does not exist in inventory.
                if(toBeSold == null) {
                    System.out.println("Product with that ID does not exist. Try again.");
                    continue;
                }
                // If out of stock, do not proceed and inform user/admin.
                if(toBeSold.getQuantity() == 0) {
                    System.out.println("We are currently out of stock of product \""+toBeSold.getItemName()+"\". Sorry!");
                    return null;
                }

                // Proper value can be returned.
                return toBeSold;

            } catch(NumberFormatException ex) {
                // User/admin typed a string, let them know proper procedure.
                System.out.println("Please enter a number for the ID of the product.");
            }
        }
    }

    // gatherQuantityToSell() will gather the amount of a product the user/admin
    // wants to purchase and return it if confirmed. Informs the user if the
    // product is out of stock or if their inputs are invalid.
    public int gatherQuantityToSell(Product toBeSold) {
        int quantityToBeSold;
        while(true) {
            System.out.print("> ");
            try {
                // Gather response from user/admin.
                String response = consoleReader.nextLine().strip().toLowerCase();
                if(response.equals("exit")) return 0;
                quantityToBeSold = Integer.parseInt(response);

                // Must sell an amount greater than 0.
                if(quantityToBeSold < 1) {
                    System.out.println("Invalid amount to sell. Value must be greater than 0. Try again.");
                    continue;
                }
                // Not enough product quantity to match user/admin request.
                else if(quantityToBeSold > toBeSold.getQuantity()) {
                    System.out.println("Value given is greater than the amount in inventory. Try again.");
                    continue;
                }

                // Can return proper value.
                return quantityToBeSold;
            } catch (NumberFormatException ex) {
                // User/admin typed a string, let them know proper procedure.
                System.out.println("Please enter a number for the quantity of product to sell.");
            }
        }
    }

    // sellProduct() involves the entire process of selling a product and
    // recording the sale. Gathers the specific product and quantity to be sold
    // of that product, if any issues occur during that process or the user/admin
    // wants to exit, return to menu. Next, the user/admin is asked to confirm the
    // sale and if the user/admin types 'Y', the transaction will be attempted.
    // The transaction follows the Atomicity principle where if there is an error
    // with updating the inventory or saving the record, the transaction will be
    // rolled back.
    public void sellProduct() {

        // If inventory is empty, exit
        if(inventory.isEmpty()) {
            System.out.println("\nThere are not products in inventory currently.");
            return;
        }

        try {
            // Do not allow auto commiting for rollback.
            dbConnect.setAutoCommit(false);

            // Gather product ID
            System.out.println("\nEnter Product ID to sell.\nEnter 'exit' to return to the menu.");
            Product toSell = gatherProductToSell();
            if(toSell == null) return;

            // Gather quantity to be sold.
            System.out.println("\nEnter quantity to sell.\nEnter 'exit' to return to the menu.");
            int quantity = gatherQuantityToSell(toSell);
            if(quantity == 0) return;

            // Confirmation before sale is made.
            System.out.format("\nAre you sure you want to buy %d of \"%s\"? (Y/N)%n", quantity, toSell.getItemName());
            while(true) {
                System.out.print("> ");
                String response = consoleReader.nextLine().strip().toLowerCase();
                if(response.equals("y")) break;
                else if(response.equals("n")) return;
                else {
                    System.out.println("Invalid Input. Enter either 'Y' or 'N'.");
                }
            }

            // Update the inventory quantity amount.
            System.out.println("\nUpdating Inventory...");
            String sellSQL = "update inventory set Quantity = Quantity - ? where InventoryID = ?;";
            PreparedStatement updateInventory = dbConnect.prepareStatement(sellSQL);
            updateInventory.setInt(1, quantity);
            updateInventory.setInt(2, toSell.getInventoryID());
            updateInventory.executeUpdate();

            // Create a sale record of the transaction.
            System.out.println("Creating a receipt...");
            Object timeParam = new java.sql.Timestamp(new Date().getTime());
            String createSaleSQL = "insert into sales (InventoryID, Quantity, SaleDate) values (?, ?, ?);";
            PreparedStatement updateSales = dbConnect.prepareStatement(createSaleSQL);
            updateSales.setInt(1, toSell.getInventoryID());
            updateSales.setInt(2, quantity);
            updateSales.setObject(3, timeParam);
            updateSales.executeUpdate();

            // Commit the transaction
            dbConnect.commit();

            System.out.println("Sale was made!");

            // Allow auto commiting and reinitialize inventory and sale records.
            dbConnect.setAutoCommit(true);
            inventory.initializeInventory();
            salesReport.initializeSalesReport();

        } catch (SQLException ex) {
            // If error occurs, roll back for Atomicity Principle.
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

    // gatherNewItemName() will gather the name for the new product that
    // will be added by the admin. Checks validity of ItemName before
    // returning. If admin wants to exit, they are allowed to end the
    // transaction early.
    public String gatherNewItemName() {
        while (true) {
            // Gather response from admin.
            System.out.print("ItemName: ");
            String name = consoleReader.nextLine().strip().toLowerCase();
            if(name.equals("exit")) return null;

            // Names can not be greater than 254 for now.
            if(name.length() > 254) {
                System.out.println("ItemName is too long. Has to be less than 254 characters.");
                continue;
            }
            // Names can not be too short to prevent unreadable acronyms. Promote detailed names.
            else if(name.length() < 5) {
                System.out.println("ItemName is expected to be greater than 4 characters. Try again.");
                continue;
            }
            // Names must be unique.
            else if(inventory.hasProductWithName(name)) {
                System.out.println("There is a product with that name already. Must have unique names.");
                continue;
            }

            // Proper value can be returned.
            return name;
        }
    }

    // gatherNewItemQuantity() will gather the quantity of the new
    // product chosen by the admin. Checks validity of quantity before
    // returning. If admin wants to exit, they are allowed to end the
    // transaction early.
    public int gatherNewItemQuantity() {
        while (true) {
            // Gather response from admin.
            System.out.print("Quantity: ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return -1;

            int value;
            try {
                value = Integer.parseInt(response);
                // No negative quantities allowed.
                if(value < 0) {
                    System.out.println("Can not have negative quantity!");
                    continue;
                }
            } catch(NumberFormatException ex) {
                // Admin typed a string, let them know proper procedure.
                System.out.println("Please enter a number for the quantity.");
                continue;
            }

            // Proper value can be returned.
            return value;
        }
    }

    // gatherNewItemPrice() will gather the price to be set of the
    // product chosen by the admin. Checks validity of price before
    // returning. If admin wants to exit, they are allowed to end the
    // transaction early.
    public double gatherNewItemPrice() {
        while (true) {
            // Gather response.
            System.out.print("Price: ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return -1;

            double value;
            try {
                value = Double.parseDouble(response);
                // No negative prices allowed.
                if(value < 0) {
                    System.out.println("Can not have negative price!");
                    continue;
                }
            } catch(NumberFormatException ex) {
                // Admin typed a string, let them know proper procedure.
                System.out.println("Please enter a number for the price.");
                continue;
            }

            // Proper value can be returned
            return value;
        }
    }

    // addProduct() will gather the new name, quantity, and price for
    // a new product that the admin is creating. Allows for exiting early
    // and canceling the process. Prompts the admin before confirmation
    // before proceeding and if the addition is confirmed, the product
    // will be added into the MySql inventory. Following insertion of product,
    // the inventory will be reinitialized to update what the MySql currently
    // has in stock.
    public void addProduct() {

        // CREATE (Crud)

        // Gather new product ItemName.
        System.out.println("\nExpecting an ItemName, Quantity, Price values.\nEnter 'exit' at anytime to return to menu.");
        String ItemName = gatherNewItemName();
        if(ItemName == null) return;

        // Gather new product Quantity.
        int quantity = gatherNewItemQuantity();
        if(quantity == -1) return;

        // Gather new product Price.
        double price = gatherNewItemPrice();
        if(price < 0) return;

        // Confirm admin intentions before proceeding.
        System.out.println("\nAre you sure you want to add the \""+ItemName+"\" entry? (Y/N)");
        while(true){
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("y")) break;
            else if(response.equals("n")) return;
            else {
                System.out.println("Please enter 'Y' or 'N'.");
            }
        }

        // Attempt to delete product.
        System.out.println("\nAttempting to add new item \""+ItemName+"\"...");
        String addSQL = "insert into inventory (ItemName, Quantity, Price) values (?, ?, ?);";
        try{

            PreparedStatement addNewItem = dbConnect.prepareStatement(addSQL);
            addNewItem.setString(1, ItemName.substring(0,1).toUpperCase() + ItemName.substring(1));
            addNewItem.setInt(2, quantity);
            addNewItem.setDouble(3, price);
            addNewItem.executeUpdate();

            // Deletion is completed, reinitialize inventory.
            System.out.println("Addition of item \""+ItemName+"\" was successful!");
            inventory.initializeInventory();

        } catch(SQLException ex) {
            // Failure with deletion, let admin know.
            System.out.println("Add New Item Failure: "+ex.getMessage());
        }

    }

    // deleteProduct() will gather the ID of the product up for deletion
    // from the admin while checking for validity of ID. If the inventory
    // is empty, the process will end immediately and the admin is informed.
    // Before deletion occurs, the admin is prompted for confirmation before
    // proceeding with deletion of product. If confirmed, the product is
    // deleted and inventory is reinitialized to get up-to-date status
    // of MySQL inventory status.
    public void deleteProduct() {

        // DELETE (cruD)

        if(inventory.isEmpty()) {
            System.out.println("\nThere are no products in inventory currently.");
            return;
        }

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

    // gatherProductToUpdate() will gather the specific product the admin
    // wants to update. If the product is not found given the ID received,
    // inform the admin. If product is found, return the product to be
    // updated. Allows early cancellation.
    public Product gatherProductToUpdate() {
        while(true) {
            // Gather response from admin.
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return null;
            try{
                int value = Integer.parseInt(response);

                // ID must be a positive value greater than 0.
                if(value < 1) {
                    System.out.println("ID can not be negative or 0. Try again.");
                    continue;
                }

                // If product returned is null, no product exists with that id.
                Product possibleNull = inventory.searchForProductByID(value);
                if(possibleNull == null) {
                    System.out.println("No item with that ID. Try again.");
                    continue;
                }

                // Proper value can be returned.
                return possibleNull;

            } catch (NumberFormatException ex) {
                // Admin typed a string, inform of proper procedure.
                System.out.println("Please enter a number for the ID.");
            }
        }
    }

    // gatherFieldToUpdate() will gather the specific field that the
    // admin wants to update from a product object or inventory entry
    // on MySQL. Validates responses and returns a proper value.
    // Allows early cancellation.
    public int gatherFieldToUpdate() {
        while(true) {
            // Gather response from admin.
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("exit")) return 4;
            try {
                int value = Integer.parseInt(response);
                // Value must be a positive value greater than 0.
                if(value < 1) {
                    System.out.println("Negative values or 0 are invalid. Try again.");
                    continue;
                }
                // Value must be less than or equal to 4 as there is only 4 choices.
                else if(value > 4) {
                    System.out.println("Value given is greater than the choices on the list. Try again.");
                    continue;
                }

                // Return proper value.
                return value;

            } catch(NumberFormatException ex) {
                // Admin typed a string, inform of proper procedure.
                System.out.println("Please enter a number for the choices above.");
            }
        }
    }

    // updateProduct() will attempt to update one field of a product
    // that the admin chooses. First, the admin chooses which product to
    // update and then which field on that specific product to update.
    // Admin is prompted to confirm their decision and if confirmed,
    // the MySQL inventory table will be updated and inventory will
    // be reinitialized to remain up-to-date.
    public void updateProduct() {

        // UPDATE (crUd)

        if(inventory.isEmpty()) {
            System.out.println("\nThere are not products in inventory currently.");
            return;
        }

        System.out.println("\nWhat is the ID of the product to update?\nEnter 'exit' to return to menu.");
        Product toBeUpdated = gatherProductToUpdate();
        if(toBeUpdated == null) return;

        System.out.println("\nWhich value should be updated?\n1. ItemName\n2. Quantity\n3. Price\n4. Exit (or type 'exit')");
        int choice = gatherFieldToUpdate();
        if(choice == 4) return;

        // Gather SQL syntax based on which product field admin wants to update.
        String updateSQL = "update inventory set ";
        switch (choice) {
            // ItemName Field
            case 1:
                System.out.println();
                String name = gatherNewItemName();
                if(name == null) return;
                updateSQL += "ItemName = '"+name.substring(0,1).toUpperCase()+name.substring(1)+
                        "' where InventoryID = "+toBeUpdated.getInventoryID()+";";
                break;
            // Quantity Field
            case 2:
                System.out.println();
                int quantity = gatherNewItemQuantity();
                if(quantity == -1) return;
                updateSQL += "Quantity = "+quantity+" where InventoryID = "+toBeUpdated.getInventoryID()+";";
                break;
            // Price Field
            case 3:
                System.out.println();
                double price = gatherNewItemPrice();
                if(price < 0) return;
                updateSQL += "Price = "+price+" where InventoryID = "+toBeUpdated.getInventoryID()+";";
                break;
            // Response Failure
            default:
                System.out.println("Response Error: choice given is outside of bounds.");
                return;
        }

        // Confirm admin intentions of updating.
        System.out.println("\nAre you sure you want to update the \""+toBeUpdated.getItemName()+"\" entry? (Y/N)");
        while(true){
            System.out.print("> ");
            String response = consoleReader.nextLine().strip().toLowerCase();
            if(response.equals("y")) break;
            else if(response.equals("n")) return;
            else {
                System.out.println("Please enter 'Y' or 'N'.");
            }
        }

        // Attempt to update product
        System.out.println("\nAttempting to update item \""+toBeUpdated.getItemName()+"\"...");
        try {

            sqlSt.executeUpdate(updateSQL);

            System.out.println("Update of item \""+toBeUpdated.getItemName()+"\" was successful!");
            inventory.initializeInventory();

        } catch(SQLException ex) {
            // SQL failure, inform admin.
            System.out.println("Update Product Failed: "+ex.getMessage());
        }

    }

    // generateInventoryReport() will print every inventory product
    // and their associated sales records that have affected their
    // quantity. Can only be called by admin.
    public void generateInventoryReport() {
        System.out.println();

        // If inventory is empty, exit and inform admin.
        if(inventory.isEmpty()) {
            System.out.println("There are no products in inventory currently.");
            return;
        }

        // Print Inventory Report!
        // For every product, print itself and the
        // sale records associated with itself.
        System.out.println("INVENTORY REPORT:");
        for(Product p: inventory.getProducts()) {
            System.out.println(p.toString());
            for(Sale s: salesReport.searchSalesInvolvingID(p.getInventoryID())) {
                System.out.println("\t- "+s.toString());
            }
        }
    }

    // close() will shutdown all connection with the
    // MySQL database and the Scanner console reader.
    // On failure, inform user.
    public void close() {
        try {
            sqlSt.close();
            dbConnect.close();
            consoleReader.close();
        } catch(SQLException ex) {
            System.out.println("Normal Close Failed: "+ex.getMessage());
        }
    }

    // Main loop of the program, separated into two
    // loops for the admin and user role with their
    // distinct functions that can be called.
    public static void main(String[] args)
    {
        IMS inventorySystem = new IMS();
        // On failed user authentication, close the program.
        if(!(inventorySystem.authenticateUser())) System.exit(0);

        // User loop, allows all basic functionality.
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
        // Admin loop, allows all basic functionality
        // and more complex processes such as addition,
        // deletion, and updating products. Plus generating
        // sales and inventory reports.
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
                        inventorySystem.updateProduct();
                        break;
                    case 7:
                        inventorySystem.generateInventoryReport();
                        break;
                    case 8:
                        inventorySystem.generateSalesReport();
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