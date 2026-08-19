package com.example.savefoodapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
public class DBAdapter {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public DBAdapter(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();

    }
}
