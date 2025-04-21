package com.example.sit305_71p.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "lostFound.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_ITEMS = "items";

    // Items Table Columns
    public static final String KEY_ITEM_ID = "id";
    public static final String KEY_ITEM_POST_TYPE = "post_type"; // "Lost" or "Found"
    public static final String KEY_ITEM_NAME = "name";
    public static final String KEY_ITEM_PHONE = "phone";
    public static final String KEY_ITEM_DESCRIPTION = "description";
    public static final String KEY_ITEM_DATE = "date";
    public static final String KEY_ITEM_LOCATION = "location";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the first time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // Define a primary key
                + KEY_ITEM_POST_TYPE + " TEXT,"
                + KEY_ITEM_NAME + " TEXT,"
                + KEY_ITEM_PHONE + " TEXT,"
                + KEY_ITEM_DESCRIPTION + " TEXT,"
                + KEY_ITEM_DATE + " TEXT,"
                + KEY_ITEM_LOCATION + " TEXT"
                + ")";
        db.execSQL(CREATE_ITEMS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // The implementation should use this method to drop tables, add tables, or do anything else it
    // needs to upgrade to the new schema version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    // --- CRUD Operations (Create, Read, Update, Delete) --- //

    // Add a new item
    public long addItem(String postType, String name, String phone, String description, String date, String location) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1; // Default value indicating failure

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_POST_TYPE, postType);
            values.put(KEY_ITEM_NAME, name);
            values.put(KEY_ITEM_PHONE, phone);
            values.put(KEY_ITEM_DESCRIPTION, description);
            values.put(KEY_ITEM_DATE, date);
            values.put(KEY_ITEM_LOCATION, location);

            // Insert row
            id = db.insertOrThrow(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            // Log error
            android.util.Log.d("DB_ERROR", "Error while trying to add item to database");
        } finally {
            db.endTransaction();
        }
        return id;
    }

    // Get all items
    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ITEMS, null, null, null, null, null, null);
    }

    // Delete an item by ID
    public int deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            rowsAffected = db.delete(TABLE_ITEMS, KEY_ITEM_ID + "=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            android.util.Log.d("DB_ERROR", "Error while trying to delete item from database");
        } finally {
            db.endTransaction();
        }
        return rowsAffected;
    }

    // Get item by ID (Optional - might be useful for detail view)
     public Cursor getItemById(long id) {
         SQLiteDatabase db = getReadableDatabase();
         return db.query(TABLE_ITEMS,
                 null, // All columns
                 KEY_ITEM_ID + " = ?", // Selection
                 new String[]{String.valueOf(id)}, // Selection args
                 null, // Group by
                 null, // Having
                 null); // Order by
     }

}
