package com.spambytes.prodstore.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "productDb")
public class Product {

    @PrimaryKey(autoGenerate = true)
    private int primary_key;

    private long barCode;
    private String name;
    private int bestBefore;

    public Product() {
    }

    @Ignore
    public Product(long barCode, String name, int bestBefore) {
        this.barCode = barCode;
        this.name = name;
        this.bestBefore = bestBefore;
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

    public int getBestBefore() {
        return bestBefore;
    }

    public void setBestBefore(int bestBefore) {
        this.bestBefore = bestBefore;
    }
}
