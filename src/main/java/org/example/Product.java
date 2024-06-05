package org.example;

public class Product {
    private int inventoryID;
    private String itemName;
    private int quantity;
    private double price;

    public Product(int inventoryID, String itemName, int quantity, double price) {
        this.inventoryID = inventoryID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getInventoryID() {
        return inventoryID;
    }

    public String getItemName() {
        return itemName;
    }


    public int getQuantity() {
        return quantity;
    }


    public double getPrice() {
        return price;
    }


    @Override
    public String toString() {
        return String.format("Product(InventoryID: %d ItemName: '%s', Quantity: %d, Price: %.2f)", inventoryID, itemName, quantity, price);
    }
}
