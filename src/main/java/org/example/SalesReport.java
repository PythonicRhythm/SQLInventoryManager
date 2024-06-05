package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SalesReport {
    private ArrayList<Sale> sales;
    private Statement sqlSt;

    public SalesReport(Statement sql) {
        this.sqlSt = sql;
        initializeSalesReport();
    }

    public ArrayList<Sale> getSales() {
        return sales;
    }

    public boolean isEmpty() {
        return sales.isEmpty();
    }

    public void initializeSalesReport() {
        sales = new ArrayList<>();
        ResultSet allSales;
        try {
            allSales = acquireSales();
            if(allSales == null) return;
            while(allSales.next()) {
                sales.add(new Sale(allSales.getInt("SaleID"),allSales.getInt("InventoryID"),
                        allSales.getInt("Quantity"),allSales.getTimestamp("SaleDate")));
            }
        } catch(SQLException ex) {
            System.out.println("Initializing Sales Report Failed: "+ex.getMessage());
        }
    }

    public ResultSet acquireSales() {
        // READ (cRud)
        String checkSalesSQL = "Select * from sales;";
        ResultSet result;
        try {
            result = sqlSt.executeQuery(checkSalesSQL);
            if(!result.isBeforeFirst()) {
                return null;
            }
            return result;
        } catch(SQLException ex) {
            System.out.println("Attempt to grab Sales Failed: "+ex.getMessage());
            return null;
        }
    }

    public void displaySales() {
        System.out.println("SALES:");
        for(int i = 0; i < sales.size(); i++) {
            System.out.printf("SaleID: %-5d InventoryID: %-5s Quantity: %-5d Price: %-30s%n",
                    sales.get(i).getSaleID(), sales.get(i).getInventoryID(),
                    sales.get(i).getQuantity(), sales.get(i).getSaleDate().toString());
        }
    }

    public ArrayList<Sale> searchSalesInvolvingID(int ID) {
        ArrayList<Sale> allSalesRelatedToID = new ArrayList<>();
        for(Sale s: sales) {
            if(s.getInventoryID() == ID)
                allSalesRelatedToID.add(s);
        }

        return allSalesRelatedToID;
    }
}
