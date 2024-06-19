package org.example;

import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class Testing {

    @Test
    public void test_proper_user_initialization() {
        User user = new User("ant", "admin");
        assertEquals("admin", user.getRole());
        assertEquals("Ant", user.getUpperName());

        user = new User("Howdy", "user");
        assertEquals("Howdy", user.getUpperName());
        assertNotEquals("howdy", user.getUpperName());
    }

    @Test
    public void test_proper_product_initialization() {
        Product product = new Product(5, "Laptop", 10, 10.99);
        assertEquals(5, product.getInventoryID());
        assertNotEquals(5, product.getInventoryID()+1);
        assertEquals("Laptop", product.getItemName());
        assertEquals(10, product.getQuantity());
        assertEquals(10.99, product.getPrice(), 0.01);
        assertEquals("Product(InventoryID: 5 ItemName: 'Laptop', Quantity: 10, Price: 10.99)", product.toString());
    }

    @Test
    public void test_proper_sale_initialization() {
        Timestamp time = new Timestamp(1000000);
        Sale sale = new Sale(1, 5, 10, time);
        assertEquals(1, sale.getSaleID());
        assertEquals(5, sale.getInventoryID());
        assertEquals(10, sale.getQuantity());
        assertEquals(new Timestamp(1000000), sale.getSaleDate());
        assertEquals(String.format("Sale(SaleID: %d, InventoryID: %d, Quantity: %d, SaleDate: %s",
                1, 5, 10, new Timestamp(1000000)), sale.toString());
    }

}
