package com.spambytes.prodstore.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.spambytes.prodstore.models.Item;


@Database(entities = {Item.class}, version = 1, exportSchema = false)
public abstract class ItemDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "itemDb";
    private static ItemDatabase sInstance;

    public static ItemDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                    ItemDatabase.class, ItemDatabase.DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return sInstance;
    }

    public abstract ItemDao ItemDao();
}
