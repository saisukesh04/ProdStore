package com.spambytes.prodstore.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "itemDb")
public class Item {

    @PrimaryKey(autoGenerate = true)
    private long primary_key;

    private String itemName, dateOfExpiry;
    private int quantity;

    public Item() {
    }

    @Ignore
    public Item(String itemName, String dateOfExpiry, int quantity) {
        this.itemName = itemName;
        this.dateOfExpiry = dateOfExpiry;
        this.quantity = quantity;
    }

    public long getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(long primary_key) {
        this.primary_key = primary_key;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
