package com.example.savefoodapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "savefood.db";
    private static final int DATABASE_VERSION = 5;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "password_salt TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "organization_id INTEGER, " +
                "latitude REAL, " +
                "longitude REAL" +
                ")";

        db.execSQL(createUsersTable);

        String createFoodOrganizationsTable = "CREATE TABLE food_organizations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "address TEXT NOT NULL, " +
                "latitude REAL, " +
                "longitude REAL" +
                ")";

        db.execSQL(createFoodOrganizationsTable);

        String createCharityOrganizationsTable = "CREATE TABLE charity_organizations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "address TEXT NOT NULL, " +
                "latitude REAL, " +
                "longitude REAL" +
                ")";

        db.execSQL(createCharityOrganizationsTable);

        String createFoodDonationsTable = "CREATE TABLE food_donations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "food_organization_id INTEGER NOT NULL, " +
                "food_name TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "description TEXT, " +
                "expiry_date TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "FOREIGN KEY(food_organization_id) REFERENCES food_organizations(id)" +
                ")";

        db.execSQL(createFoodDonationsTable);

        String createDonationRequestsTable = "CREATE TABLE donation_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "donation_id INTEGER NOT NULL, " +
                "charity_organization_id INTEGER NOT NULL, " +
                "quantity_requested INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " +
                "FOREIGN KEY(donation_id) REFERENCES food_donations(id), " +
                "FOREIGN KEY(charity_organization_id) REFERENCES charity_organizations(id)" +
                ")";

        db.execSQL(createDonationRequestsTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS donation_requests");
        db.execSQL("DROP TABLE IF EXISTS food_donations");
        db.execSQL("DROP TABLE IF EXISTS charity_organizations");
        db.execSQL("DROP TABLE IF EXISTS food_organizations");
        db.execSQL("DROP TABLE IF EXISTS users");

        onCreate(db);
    }
}