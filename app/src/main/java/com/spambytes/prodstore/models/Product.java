package com.spambytes.prodstore.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "productDb")
public class Product {

    @PrimaryKey(autoGenerate = true)
    private int primary_key;

    private long barCode;
    private String name, dateOfManufacture;
    private int quantity;

    public Product() {
    }

    @Ignore
    public Product(int primary_key, long barCode, String name, String dateOfManufacture, int quantity) {
        this.primary_key = primary_key;
        this.barCode = barCode;
        this.name = name;
        this.dateOfManufacture = dateOfManufacture;
        this.quantity = quantity;
    }

    public int getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(int primary_key) {
        this.primary_key = primary_key;
    }

    public long getBarCode() {
        return barCode;
    }

    public void setBarCode(long barCode) {
        this.barCode = barCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfManufacture() {
        return dateOfManufacture;
    }

    public void setDateOfManufacture(String dateOfManufacture) {
        this.dateOfManufacture = dateOfManufacture;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
