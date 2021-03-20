package com.spambytes.prodstore.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.spambytes.prodstore.models.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insertProduct(Product product);

    @Query("DELETE FROM productDb")
    void clearProductDb();

    @Query("SELECT * FROM productDb")
    List<Product> loadAllProducts();

    @Query("SELECT COUNT(*) FROM productDb")
    int countProducts();

    @Query("SELECT name FROM productDb WHERE barCode = :barcode")
    String fetchProduct(String barcode);

    @Query("SELECT bestBefore FROM productDb WHERE barCode = :barcode")
    int fetchBestBefore(String barcode);
}
