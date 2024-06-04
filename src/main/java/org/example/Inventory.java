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
                products.add(new Product(allProducts.getString("ItemName"),
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
            System.out.printf("ID: %-5d %s %n", (i+1), products.get(i).toString());
        }
    }
}
