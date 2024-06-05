package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Inventory {
    private ArrayList<Product> products;
    private Statement sqlSt;

    public Inventory(Statement sql) {
        this.sqlSt = sql;
        initializeInventory();
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public void initializeInventory() {
        products = new ArrayList<>();
        ResultSet allProducts;
        try {
            allProducts = acquireInventory();
            if(allProducts == null) return;
            while(allProducts.next()) {
                products.add(new Product(allProducts.getInt("InventoryID"), allProducts.getString("ItemName"),
                        allProducts.getInt("Quantity"), allProducts.getFloat("Price")));
            }
        } catch(SQLException ex) {
            System.out.println("Initializing Inventory Failed: "+ex.getMessage());
        }

    }

    public ResultSet acquireInventory() {
        String checkUserSQL = "Select * from inventory;";
        ResultSet result;
        try {
            result = sqlSt.executeQuery(checkUserSQL);
            if(!result.isBeforeFirst()) {
                return null;
            }
            return result;
        } catch(SQLException ex) {
            System.out.println("Attempt to grab Inventory Failed: "+ex.getMessage());
            return null;
        }
    }

    public void displayInventory() {
        for(int i = 0; i < products.size(); i++) {
            System.out.printf("ID: %-5d Item: %-15s Quantity: %-5d Price: %-6.2f%n",
                    products.get(i).getInventoryID(), products.get(i).getItemName(),
                    products.get(i).getQuantity(), products.get(i).getPrice());
        }
    }

    public Product searchForProductByName(String name) {
        for(Product p: products) {
            if(name.equals(p.getItemName().toLowerCase()))
                return p;
        }
        return null;
    }

    public Product searchForProductByID(int id) {
        for(Product p: products) {
            if(p.getInventoryID() == id)
                return p;
        }
        return null;
    }

    public boolean hasProductWithName(String name) {
        for(Product p: products) {
            if(p.getItemName().toLowerCase().equals(name))
                return true;
        }
        return false;
    }
}
