package com.spambytes.prodstore.database;

import androidx.room.Dao;
import androidx.room.Insert;

import com.spambytes.prodstore.models.Item;

@Dao
public interface ItemDao {

    @Insert
    void insertItem(Item item);
}
