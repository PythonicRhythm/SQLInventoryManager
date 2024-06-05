package org.example;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Sale {
    private final int saleID;
    private final int inventoryID;
    private final int quantity;
    private final Timestamp saleDate;

    public Sale(int saleID, int inventoryID, int quantity, Timestamp saleDate) {
        this.saleID = saleID;
        this.inventoryID = inventoryID;
        this.quantity = quantity;
        this.saleDate = saleDate;
    }

    public int getSaleID() {
        return saleID;
    }

    public int getInventoryID() {
        return inventoryID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }
}
