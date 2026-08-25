package com.example.savefoodapp.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "savefood.db";

    // Increase this number only when the database structure changes
    private static final int DATABASE_VERSION = 7;

    public DatabaseHelper(Context context) {
        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {

        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ====================================================
        // USERS
        // ====================================================

        String createUsersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
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

        // ====================================================
        // FOOD ORGANIZATIONS
        // ====================================================

        String createFoodOrganizationsTable =
                "CREATE TABLE IF NOT EXISTS food_organizations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "address TEXT NOT NULL, " +
                        "latitude REAL, " +
                        "longitude REAL" +
                        ")";

        db.execSQL(createFoodOrganizationsTable);

        // ====================================================
        // CHARITY ORGANIZATIONS
        // ====================================================

        String createCharityOrganizationsTable =
                "CREATE TABLE IF NOT EXISTS charity_organizations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "address TEXT NOT NULL, " +
                        "latitude REAL, " +
                        "longitude REAL" +
                        ")";

        db.execSQL(createCharityOrganizationsTable);

        // ====================================================
        // FOOD DONATIONS
        // ====================================================

        String createFoodDonationsTable =
                "CREATE TABLE IF NOT EXISTS food_donations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "food_organization_id INTEGER NOT NULL, " +
                        "food_name TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "description TEXT, " +
                        "expiry_date TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "image_path TEXT, " +
                        "FOREIGN KEY(food_organization_id) " +
                        "REFERENCES food_organizations(id)" +
                        ")";

        db.execSQL(createFoodDonationsTable);

        // ====================================================
        // DONATION REQUESTS
        // ====================================================

        String createDonationRequestsTable =
                "CREATE TABLE IF NOT EXISTS donation_requests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "donation_id INTEGER NOT NULL, " +
                        "charity_organization_id INTEGER NOT NULL, " +
                        "quantity_requested INTEGER NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "FOREIGN KEY(donation_id) " +
                        "REFERENCES food_donations(id), " +
                        "FOREIGN KEY(charity_organization_id) " +
                        "REFERENCES charity_organizations(id)" +
                        ")";

        db.execSQL(createDonationRequestsTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        /*
         * IMPORTANT:
         *
         * Do NOT drop tables here.
         *
         * Dropping tables would delete:
         * - Users
         * - Organizations
         * - Food offers
         * - Donation requests
         *
         * The current database schema does not require
         * destructive migration.
         *
         * Make future database changes here using
         * ALTER TABLE / CREATE TABLE IF NOT EXISTS
         * when necessary.
         */

        if (oldVersion < 7) {

            // Ensure all current tables exist.
            onCreate(db);
        }
    }
}