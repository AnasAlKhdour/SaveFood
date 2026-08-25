package com.example.savefoodapp.data.database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.security.PasswordUtils;
import com.example.savefoodapp.data.models.DonationRequest;

public class DBAdapter {

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public DBAdapter(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = databaseHelper.getWritableDatabase();
    }

    // T2.8 - Insert User
    public long insertUser(User user) {

        String salt = PasswordUtils.generateSalt();

        String passwordHash = PasswordUtils.hashPassword(
                user.getPassword(),
                salt
        );

        ContentValues values = new ContentValues();

        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("password_hash", passwordHash);
        values.put("password_salt", salt);
        values.put("role", user.getRole());

        if (user.getOrganizationId() > 0) {
            values.put("organization_id", user.getOrganizationId());
        }

        return database.insert(
                "users",
                null,
                values
        );
    }

    // T2.9 - Get User by Email
    public User getUser(String email) {

        String query =
                "SELECT id, name, email, password_hash, password_salt, role, " +
                        "organization_id, latitude, longitude " +
                        "FROM users WHERE email = ?";

        Cursor cursor = database.rawQuery(
                query,
                new String[]{email}
        );

        if (cursor.moveToFirst()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            String userEmail = cursor.getString(
                    cursor.getColumnIndexOrThrow("email")
            );

            String passwordHash = cursor.getString(
                    cursor.getColumnIndexOrThrow("password_hash")
            );

            String passwordSalt = cursor.getString(
                    cursor.getColumnIndexOrThrow("password_salt")
            );

            String role = cursor.getString(
                    cursor.getColumnIndexOrThrow("role")
            );

            int organizationId = 0;

            int organizationColumnIndex =
                    cursor.getColumnIndexOrThrow("organization_id");

            if (!cursor.isNull(organizationColumnIndex)) {
                organizationId =
                        cursor.getInt(organizationColumnIndex);
            }

            double latitude = 0.0;
            double longitude = 0.0;

            int latitudeIndex =
                    cursor.getColumnIndexOrThrow("latitude");

            if (!cursor.isNull(latitudeIndex)) {
                latitude =
                        cursor.getDouble(latitudeIndex);
            }

            int longitudeIndex =
                    cursor.getColumnIndexOrThrow("longitude");

            if (!cursor.isNull(longitudeIndex)) {
                longitude =
                        cursor.getDouble(longitudeIndex);
            }

            User user =
                    new User(
                            id,
                            name,
                            userEmail,
                            passwordHash,
                            passwordSalt,
                            role,
                            organizationId
                    );

            user.setLatitude(latitude);
            user.setLongitude(longitude);

            cursor.close();

            return user;
        }

        cursor.close();

        return null;
    }

    // T2.11 - Update User
    public int updateUser(User user) {

        ContentValues values = new ContentValues();

        values.put(
                "name",
                user.getName()
        );

        values.put(
                "email",
                user.getEmail()
        );

        values.put(
                "role",
                user.getRole()
        );

        if (user.getOrganizationId() > 0) {

            values.put(
                    "organization_id",
                    user.getOrganizationId()
            );

        } else {

            values.putNull(
                    "organization_id"
            );
        }

        return database.update(
                "users",
                values,
                "id = ?",
                new String[]{
                        String.valueOf(
                                user.getId()
                        )
                }
        );
    }

    // T2.15 - Update User Location
    public int updateUserLocation(
            int userId,
            double latitude,
            double longitude
    ) {

        ContentValues values = new ContentValues();

        values.put(
                "latitude",
                latitude
        );

        values.put(
                "longitude",
                longitude
        );

        return database.update(
                "users",
                values,
                "id = ?",
                new String[]{
                        String.valueOf(userId)
                }
        );
    }

    // ====================================================
// UPDATE CHARITY ORGANIZATION LOCATION
// ====================================================

    public boolean updateCharityOrganizationLocation(
            int userId,
            int organizationId,
            double latitude,
            double longitude
    ) {

        database.beginTransaction();

        Cursor cursor = null;

        try {

            // ------------------------------------------------
            // Validate User + Organization Ownership
            // ------------------------------------------------

            String query =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ? " +
                            "LIMIT 1";

            cursor =
                    database.rawQuery(
                            query,
                            new String[]{
                                    String.valueOf(userId)
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String role =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            cursor.close();
            cursor = null;

            if (!"Charity Organization".equals(
                    role
            )) {

                return false;
            }

            if (userOrganizationId
                    != organizationId) {

                return false;
            }

            // ------------------------------------------------
            // Update Organization Location
            // ------------------------------------------------

            ContentValues organizationValues =
                    new ContentValues();

            organizationValues.put(
                    "latitude",
                    latitude
            );

            organizationValues.put(
                    "longitude",
                    longitude
            );

            int organizationRows =
                    database.update(
                            "charity_organizations",
                            organizationValues,
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Update User Location
            // ------------------------------------------------

            ContentValues userValues =
                    new ContentValues();

            userValues.put(
                    "latitude",
                    latitude
            );

            userValues.put(
                    "longitude",
                    longitude
            );

            int userRows =
                    database.update(
                            "users",
                            userValues,
                            "id = ? " +
                                    "AND organization_id = ?",
                            new String[]{
                                    String.valueOf(userId),
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (userRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Commit
            // ------------------------------------------------

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    // T3.3 - Insert Food Donation
    public long insertFoodDonation(
            FoodDonation donation
    ) {

        ContentValues values =
                new ContentValues();

        values.put(
                "food_organization_id",
                donation.getFoodOrganizationId()
        );

        values.put(
                "food_name",
                donation.getFoodName()
        );

        values.put(
                "quantity",
                donation.getQuantity()
        );

        values.put(
                "description",
                donation.getDescription()
        );

        values.put(
                "expiry_date",
                donation.getExpiryDate()
        );

        values.put(
                "status",
                donation.getStatus()
        );

        values.put(
                "image_path",
                donation.getImagePath()
        );

        return database.insert(
                "food_donations",
                null,
                values
        );
    }

    // T3.4 - Get Food Donations by Organization
    public java.util.List<FoodDonation>
    getFoodDonationsByOrganizationId(
            int organizationId
    ) {

        java.util.List<FoodDonation> donations =
                new java.util.ArrayList<>();

        String query =
                "SELECT id, food_organization_id, food_name, quantity, " +
                        "description, expiry_date, status, image_path " +
                        "FROM food_donations " +
                        "WHERE food_organization_id = ? " +
                        "ORDER BY id DESC";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        organizationId
                                )
                        }
                );

        while (cursor.moveToNext()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                    );

            int foodOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "food_organization_id"
                            )
                    );

            String foodName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "food_name"
                            )
                    );

            int quantity =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            String description =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );

            String expiryDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "expiry_date"
                            )
                    );

            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            String imagePath =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "image_path"
                            )
                    );

            FoodDonation donation =
                    new FoodDonation(
                            id,
                            foodOrganizationId,
                            foodName,
                            quantity,
                            description,
                            expiryDate,
                            status,
                            imagePath
                    );

            donations.add(donation);
        }

        cursor.close();

        return donations;
    }

    // T3.5 - Get Food Donation by ID
    public FoodDonation getFoodDonationById(
            int donationId
    ) {

        String query =
                "SELECT id, food_organization_id, food_name, quantity, " +
                        "description, expiry_date, status, image_path " +
                        "FROM food_donations " +
                        "WHERE id = ?";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        donationId
                                )
                        }
                );

        if (cursor.moveToFirst()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                    );

            int foodOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "food_organization_id"
                            )
                    );

            String foodName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "food_name"
                            )
                    );

            int quantity =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            String description =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );

            String expiryDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "expiry_date"
                            )
                    );

            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            String imagePath =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "image_path"
                            )
                    );

            FoodDonation donation =
                    new FoodDonation(
                            id,
                            foodOrganizationId,
                            foodName,
                            quantity,
                            description,
                            expiryDate,
                            status,
                            imagePath
                    );

            cursor.close();

            return donation;
        }

        cursor.close();

        return null;
    }

    // ====================================================
// UPDATE FOOD DONATION
// ====================================================

    public int updateFoodDonation(
            int donationId,
            int foodOrganizationId,
            String foodName,
            int quantity,
            String description,
            String expiryDate,
            String imagePath
    ) {

        ContentValues values =
                new ContentValues();

        values.put(
                "food_name",
                foodName
        );

        values.put(
                "quantity",
                quantity
        );

        values.put(
                "description",
                description
        );

        values.put(
                "expiry_date",
                expiryDate
        );

        // ------------------------------------------------
        // Update Image Path
        // ------------------------------------------------

        values.put(
                "image_path",
                imagePath
        );

        // ------------------------------------------------
        // Keep Status Synchronized
        // ------------------------------------------------
        //
        // AVAILABLE:
        // - quantity > 0
        // - expiry date is today or later
        //
        // UNAVAILABLE:
        // - quantity == 0
        // OR
        // - expiry date has passed
        // ------------------------------------------------

        String today =
                new java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.US
                ).format(
                        new java.util.Date()
                );

        String newStatus;

        if (quantity > 0
                && expiryDate.compareTo(
                today
        ) >= 0) {

            newStatus =
                    "AVAILABLE";

        } else {

            newStatus =
                    "UNAVAILABLE";
        }

        values.put(
                "status",
                newStatus
        );

        // ------------------------------------------------
        // Update Only If Offer Belongs
        // To Current Food Organization
        // ------------------------------------------------

        return database.update(
                "food_donations",
                values,
                "id = ? AND food_organization_id = ?",
                new String[]{
                        String.valueOf(
                                donationId
                        ),
                        String.valueOf(
                                foodOrganizationId
                        )
                }
        );
    }

    // T3.3 - Insert Food Organization
    public long insertFoodOrganization(
            String name,
            String phone,
            String address,
            double latitude,
            double longitude
    ) {

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                name
        );

        values.put(
                "phone",
                phone
        );

        values.put(
                "address",
                address
        );

        values.put(
                "latitude",
                latitude
        );

        values.put(
                "longitude",
                longitude
        );

        return database.insert(
                "food_organizations",
                null,
                values
        );
    }

    // Insert Charity Organization
    public long insertCharityOrganization(
            String name,
            String phone,
            String address,
            double latitude,
            double longitude
    ) {

        ContentValues values =
                new ContentValues();

        values.put(
                "name",
                name
        );

        values.put(
                "phone",
                phone
        );

        values.put(
                "address",
                address
        );

        values.put(
                "latitude",
                latitude
        );

        values.put(
                "longitude",
                longitude
        );

        return database.insert(
                "charity_organizations",
                null,
                values
        );
    }

    // T7.1 - Get Charity Organization Details
    public String[] getCharityOrganizationDetails(
            int organizationId
    ) {

        String query =
                "SELECT name, phone, address, latitude, longitude " +
                        "FROM charity_organizations " +
                        "WHERE id = ? " +
                        "LIMIT 1";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        organizationId
                                )
                        }
                );

        if (cursor.moveToFirst()) {

            String name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "name"
                            )
                    );

            String phone =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "phone"
                            )
                    );

            String address =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "address"
                            )
                    );

            double latitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "latitude"
                            )
                    );

            double longitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "longitude"
                            )
                    );

            cursor.close();

            return new String[]{
                    name,
                    phone,
                    address,
                    String.valueOf(latitude),
                    String.valueOf(longitude)
            };
        }

        cursor.close();

        return null;
    }

    // T7.2 - Update Charity Profile
    public boolean updateCharityProfile(
            int userId,
            int organizationId,
            String name,
            String phone
    ) {

        database.beginTransaction();

        Cursor cursor = null;

        try {

            // ------------------------------------------------
            // Validate User
            // ------------------------------------------------

            String userQuery =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ?";

            cursor =
                    database.rawQuery(
                            userQuery,
                            new String[]{
                                    String.valueOf(
                                            userId
                                    )
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String role =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            cursor.close();
            cursor = null;

            // ------------------------------------------------
            // Validate Role
            // ------------------------------------------------

            if (!"Charity Organization".equals(
                    role
            )) {

                return false;
            }

            // ------------------------------------------------
            // Validate Organization Ownership
            // ------------------------------------------------

            if (userOrganizationId
                    != organizationId) {

                return false;
            }

            // ------------------------------------------------
            // Update Organization
            // ------------------------------------------------

            ContentValues organizationValues =
                    new ContentValues();

            organizationValues.put(
                    "name",
                    name
            );

            organizationValues.put(
                    "phone",
                    phone
            );

            int organizationRows =
                    database.update(
                            "charity_organizations",
                            organizationValues,
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Keep User Name Synchronized
            // ------------------------------------------------

            ContentValues userValues =
                    new ContentValues();

            userValues.put(
                    "name",
                    name
            );

            int userRows =
                    database.update(
                            "users",
                            userValues,
                            "id = ? " +
                                    "AND organization_id = ?",
                            new String[]{
                                    String.valueOf(userId),
                                    String.valueOf(organizationId)
                            }
                    );

            if (userRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Commit
            // ------------------------------------------------

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    // T3.3 - Link user to organization
    public int updateUserOrganizationId(
            String email,
            int organizationId
    ) {

        ContentValues values =
                new ContentValues();

        values.put(
                "organization_id",
                organizationId
        );

        return database.update(
                "users",
                values,
                "email = ?",
                new String[]{email}
        );
    }

    // T3.6 - Delete Food Donation
    public int deleteFoodDonation(
            int donationId,
            int foodOrganizationId
    ) {

        database.beginTransaction();

        try {

            // Delete related requests only if
            // the donation belongs to this organization.
            database.delete(
                    "donation_requests",
                    "donation_id = ? " +
                            "AND donation_id IN (" +
                            "SELECT id FROM food_donations " +
                            "WHERE id = ? " +
                            "AND food_organization_id = ?" +
                            ")",
                    new String[]{
                            String.valueOf(donationId),
                            String.valueOf(donationId),
                            String.valueOf(foodOrganizationId)
                    }
            );

            // Delete the donation only if it belongs
            // to the current food organization.
            int result =
                    database.delete(
                            "food_donations",
                            "id = ? AND food_organization_id = ?",
                            new String[]{
                                    String.valueOf(donationId),
                                    String.valueOf(foodOrganizationId)
                            }
                    );

            database.setTransactionSuccessful();

            return result;

        } finally {

            database.endTransaction();
        }
    }

    // T5.2 - Get Available Offers
    public java.util.List<FoodDonation>
    getAvailableOffers() {

        java.util.List<FoodDonation> offers =
                new java.util.ArrayList<>();

        /*
         * Only return offers that:
         * 1. Have AVAILABLE status.
         * 2. Have an expiry date that is today
         *    or later.
         *
         * The date format stored in the database
         * is YYYY-MM-DD, so SQLite can compare it
         * safely with date('now', 'localtime').
         */
        String query =
                "SELECT id, food_organization_id, food_name, quantity, " +
                        "description, expiry_date, status, image_path " +
                        "FROM food_donations " +
                        "WHERE status = ? " +
                        "AND expiry_date >= date('now', 'localtime') " +
                        "ORDER BY id DESC";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                "AVAILABLE"
                        }
                );

        while (cursor.moveToNext()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                    );

            int foodOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "food_organization_id"
                            )
                    );

            String foodName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "food_name"
                            )
                    );

            int quantity =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            String description =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "description"
                            )
                    );

            String expiryDate =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "expiry_date"
                            )
                    );

            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            String imagePath =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "image_path"
                            )
                    );

            FoodDonation donation =
                    new FoodDonation(
                            id,
                            foodOrganizationId,
                            foodName,
                            quantity,
                            description,
                            expiryDate,
                            status,
                            imagePath
                    );

            offers.add(donation);
        }

        cursor.close();

        return offers;
    }

    // T5.7 - Get Food Organization Location
    public double[] getOrganizationLocation(
            int organizationId
    ) {

        String query =
                "SELECT latitude, longitude " +
                        "FROM food_organizations " +
                        "WHERE id = ? " +
                        "AND latitude IS NOT NULL " +
                        "AND longitude IS NOT NULL " +
                        "LIMIT 1";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        organizationId
                                )
                        }
                );

        if (cursor.moveToFirst()) {

            double latitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "latitude"
                            )
                    );

            double longitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "longitude"
                            )
                    );

            cursor.close();

            return new double[]{
                    latitude,
                    longitude
            };
        }

        cursor.close();

        return null;
    }

    // ====================================================
    // SPRINT 6 - REQUESTS
    // ====================================================

    // T6.10 - Insert Donation Request
    public long insertRequest(
            DonationRequest request,
            int userId
    ) {

        database.beginTransaction();

        Cursor userCursor = null;
        Cursor donationCursor = null;

        try {

            // ====================================================
            // Validate Request
            // ====================================================

            if (request == null) {
                return -1;
            }

            if (userId <= 0) {
                return -1;
            }

            if (request.getDonationId() <= 0) {
                return -1;
            }

            if (request.getCharityOrganizationId() <= 0) {
                return -1;
            }

            if (request.getQuantityRequested() <= 0) {
                return -1;
            }

            // ====================================================
            // Verify Logged-in User
            // ====================================================

            String userQuery =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ?";

            userCursor =
                    database.rawQuery(
                            userQuery,
                            new String[]{
                                    String.valueOf(
                                            userId
                                    )
                            }
                    );

            if (!userCursor.moveToFirst()) {
                return -1;
            }

            String role =
                    userCursor.getString(
                            userCursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    userCursor.getInt(
                            userCursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            // ----------------------------------------------------
            // User must be a Charity Organization
            // ----------------------------------------------------

            if (!"Charity Organization".equals(
                    role
            )) {

                return -1;
            }

            // ----------------------------------------------------
            // User must belong to the same Charity Organization
            // stored in the request
            // ----------------------------------------------------

            if (userOrganizationId
                    != request.getCharityOrganizationId()) {

                return -1;
            }

            // ====================================================
            // Validate Current Donation
            // ====================================================

            String donationQuery =
                    "SELECT quantity, status " +
                            "FROM food_donations " +
                            "WHERE id = ? " +
                            "AND status = ? " +
                            "AND expiry_date >= date('now', 'localtime')";

            donationCursor =
                    database.rawQuery(
                            donationQuery,
                            new String[]{
                                    String.valueOf(
                                            request.getDonationId()
                                    ),
                                    "AVAILABLE"
                            }
                    );

            if (!donationCursor.moveToFirst()) {
                return -1;
            }

            int currentQuantity =
                    donationCursor.getInt(
                            donationCursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            // ====================================================
            // Validate Requested Quantity
            // ====================================================

            if (request.getQuantityRequested()
                    > currentQuantity) {

                return -1;
            }

            // ====================================================
            // Insert Request
            // ====================================================

            ContentValues values =
                    new ContentValues();

            values.put(
                    "donation_id",
                    request.getDonationId()
            );

            values.put(
                    "charity_organization_id",
                    request.getCharityOrganizationId()
            );

            values.put(
                    "quantity_requested",
                    request.getQuantityRequested()
            );

            values.put(
                    "status",
                    "PENDING"
            );

            long requestId =
                    database.insert(
                            "donation_requests",
                            null,
                            values
                    );

            if (requestId == -1) {
                return -1;
            }

            database.setTransactionSuccessful();

            return requestId;

        } finally {

            if (userCursor != null) {
                userCursor.close();
            }

            if (donationCursor != null) {
                donationCursor.close();
            }

            database.endTransaction();
        }
    }

    // T6.11 - Get Requests by Charity
    public java.util.List<DonationRequest>
    getRequestsByCharity(
            int charityOrganizationId
    ) {

        java.util.List<DonationRequest> requests =
                new java.util.ArrayList<>();

        String query =
                "SELECT id, donation_id, charity_organization_id, " +
                        "quantity_requested, status " +
                        "FROM donation_requests " +
                        "WHERE charity_organization_id = ? " +
                        "ORDER BY id DESC";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        charityOrganizationId
                                )
                        }
                );

        while (cursor.moveToNext()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "id"
                            )
                    );

            int donationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "donation_id"
                            )
                    );

            int charityId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "charity_organization_id"
                            )
                    );

            int quantityRequested =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity_requested"
                            )
                    );

            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            DonationRequest request =
                    new DonationRequest(
                            id,
                            donationId,
                            charityId,
                            quantityRequested,
                            status
                    );

            requests.add(request);
        }

        cursor.close();

        return requests;
    }

    // ====================================================
// T6.14 - Cancel Request by Charity
// ====================================================

    public int cancelRequest(
            int requestId,
            int charityOrganizationId
    ) {

        if (requestId <= 0
                || charityOrganizationId <= 0) {

            return 0;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                "CANCELLED"
        );

        /*
         * A request can be cancelled only when:
         *
         * 1. It belongs to the current charity organization.
         * 2. Its current status is PENDING.
         */
        String whereClause =
                "id = ? " +
                        "AND charity_organization_id = ? " +
                        "AND status = ?";

        return database.update(
                "donation_requests",
                values,
                whereClause,
                new String[]{
                        String.valueOf(
                                requestId
                        ),
                        String.valueOf(
                                charityOrganizationId
                        ),
                        "PENDING"
                }
        );
    }

    // T6.12 - Get Requests by Food Institution
    public java.util.List<DonationRequest>
    getRequestsByInstitution(
            int foodOrganizationId
    ) {

        java.util.List<DonationRequest> requests =
                new java.util.ArrayList<>();

        String query =
                "SELECT dr.id, dr.donation_id, " +
                        "dr.charity_organization_id, " +
                        "dr.quantity_requested, dr.status " +
                        "FROM donation_requests dr " +
                        "INNER JOIN food_donations fd " +
                        "ON dr.donation_id = fd.id " +
                        "WHERE fd.food_organization_id = ? " +
                        "ORDER BY dr.id DESC";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        foodOrganizationId
                                )
                        }
                );

        while (cursor.moveToNext()) {

            int id =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "id"
                            )
                    );

            int donationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "donation_id"
                            )
                    );

            int charityId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "charity_organization_id"
                            )
                    );

            int quantityRequested =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity_requested"
                            )
                    );

            String status =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            DonationRequest request =
                    new DonationRequest(
                            id,
                            donationId,
                            charityId,
                            quantityRequested,
                            status
                    );

            requests.add(request);
        }

        cursor.close();

        return requests;
    }

    // T6.13 - Update Request Status
    public int updateRequestStatus(
            int requestId,
            int foodOrganizationId,
            String status
    ) {

        if (requestId <= 0
                || foodOrganizationId <= 0
                || status == null) {

            return 0;
        }

        /*
         * The request can only be updated if:
         * - the request belongs to the specified food organization
         *   through its related donation
         * - the request is still PENDING
         */
        String whereClause =
                "id = ? " +
                        "AND status = ? " +
                        "AND donation_id IN (" +
                        "SELECT id " +
                        "FROM food_donations " +
                        "WHERE food_organization_id = ?" +
                        ")";

        ContentValues values =
                new ContentValues();

        values.put(
                "status",
                status
        );

        return database.update(
                "donation_requests",
                values,
                whereClause,
                new String[]{
                        String.valueOf(
                                requestId
                        ),
                        "PENDING",
                        String.valueOf(
                                foodOrganizationId
                        )
                }
        );
    }

    // T6.8 - Accept Request and Update Donation Quantity
    // T6.8 - Accept Request and Update Donation Quantity
    public boolean acceptRequest(
            int requestId,
            int foodOrganizationId
    ) {

        database.beginTransaction();

        Cursor requestCursor = null;
        Cursor donationCursor = null;

        try {

            if (requestId <= 0
                    || foodOrganizationId <= 0) {

                return false;
            }

            // ====================================================
            // Get Request
            // ====================================================

            String requestQuery =
                    "SELECT dr.donation_id, " +
                            "dr.quantity_requested, " +
                            "dr.status " +
                            "FROM donation_requests dr " +
                            "INNER JOIN food_donations fd " +
                            "ON dr.donation_id = fd.id " +
                            "WHERE dr.id = ? " +
                            "AND fd.food_organization_id = ?";

            requestCursor =
                    database.rawQuery(
                            requestQuery,
                            new String[]{
                                    String.valueOf(
                                            requestId
                                    ),
                                    String.valueOf(
                                            foodOrganizationId
                                    )
                            }
                    );

            if (!requestCursor.moveToFirst()) {
                return false;
            }

            int donationId =
                    requestCursor.getInt(
                            requestCursor.getColumnIndexOrThrow(
                                    "donation_id"
                            )
                    );

            int quantityRequested =
                    requestCursor.getInt(
                            requestCursor.getColumnIndexOrThrow(
                                    "quantity_requested"
                            )
                    );

            String requestStatus =
                    requestCursor.getString(
                            requestCursor.getColumnIndexOrThrow(
                                    "status"
                            )
                    );

            // ====================================================
            // Request Must Be PENDING
            // ====================================================

            if (!"PENDING".equalsIgnoreCase(
                    requestStatus
            )) {

                return false;
            }

            // ====================================================
            // Get Current Donation
            // ====================================================

            String donationQuery =
                    "SELECT quantity " +
                            "FROM food_donations " +
                            "WHERE id = ? " +
                            "AND food_organization_id = ? " +
                            "AND status = ? " +
                            "AND expiry_date >= date('now', 'localtime')";

            donationCursor =
                    database.rawQuery(
                            donationQuery,
                            new String[]{
                                    String.valueOf(
                                            donationId
                                    ),
                                    String.valueOf(
                                            foodOrganizationId
                                    ),
                                    "AVAILABLE"
                            }
                    );

            if (!donationCursor.moveToFirst()) {
                return false;
            }

            int currentQuantity =
                    donationCursor.getInt(
                            donationCursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            // ====================================================
            // Validate Quantity
            // ====================================================

            if (quantityRequested
                    > currentQuantity) {

                return false;
            }

            // ====================================================
            // Calculate Remaining Quantity
            // ====================================================

            int remainingQuantity =
                    currentQuantity
                            - quantityRequested;

            ContentValues donationValues =
                    new ContentValues();

            donationValues.put(
                    "quantity",
                    remainingQuantity
            );

            if (remainingQuantity == 0) {

                donationValues.put(
                        "status",
                        "UNAVAILABLE"
                );
            }

            // ====================================================
            // Update Donation
            // ====================================================

            int donationUpdated =
                    database.update(
                            "food_donations",
                            donationValues,
                            "id = ? " +
                                    "AND food_organization_id = ?",
                            new String[]{
                                    String.valueOf(
                                            donationId
                                    ),
                                    String.valueOf(
                                            foodOrganizationId
                                    )
                            }
                    );

            if (donationUpdated <= 0) {
                return false;
            }

            // ====================================================
            // Update Request
            // ====================================================

            ContentValues requestValues =
                    new ContentValues();

            requestValues.put(
                    "status",
                    "ACCEPTED"
            );

            int requestUpdated =
                    database.update(
                            "donation_requests",
                            requestValues,
                            "id = ? " +
                                    "AND status = ? " +
                                    "AND donation_id = ?",
                            new String[]{
                                    String.valueOf(
                                            requestId
                                    ),
                                    "PENDING",
                                    String.valueOf(
                                            donationId
                                    )
                            }
                    );

            if (requestUpdated <= 0) {
                return false;
            }

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (requestCursor != null) {
                requestCursor.close();
            }

            if (donationCursor != null) {
                donationCursor.close();
            }

            database.endTransaction();
        }
    }
    // ====================================================
// COMPLETE REGISTRATION TRANSACTION
// ====================================================

    public long registerOrganizationAndUser(
            String organizationType,
            String name,
            String phone,
            String address,
            double latitude,
            double longitude,
            String email,
            String password,
            String role
    ) {

        database.beginTransaction();

        try {

            // ------------------------------------------------
            // Validate input
            // ------------------------------------------------

            if (name == null || name.trim().isEmpty()) {
                return -1;
            }

            if (phone == null || phone.trim().isEmpty()) {
                return -1;
            }

            if (email == null || email.trim().isEmpty()) {
                return -1;
            }

            if (password == null || password.isEmpty()) {
                return -1;
            }

            if (role == null || role.trim().isEmpty()) {
                return -1;
            }

            if (organizationType == null
                    || organizationType.trim().isEmpty()) {
                return -1;
            }

            // ------------------------------------------------
            // Step 1 - Create Organization
            // ------------------------------------------------

            ContentValues organizationValues =
                    new ContentValues();

            organizationValues.put(
                    "name",
                    name
            );

            organizationValues.put(
                    "phone",
                    phone
            );

            organizationValues.put(
                    "address",
                    address
            );

            organizationValues.put(
                    "latitude",
                    latitude
            );

            organizationValues.put(
                    "longitude",
                    longitude
            );

            String organizationTable;

            if ("Food Institution".equals(
                    organizationType
            )) {

                organizationTable =
                        "food_organizations";

            } else if ("Charity Organization".equals(
                    organizationType
            )) {

                organizationTable =
                        "charity_organizations";

            } else {

                return -1;
            }

            long organizationId =
                    database.insert(
                            organizationTable,
                            null,
                            organizationValues
                    );

            if (organizationId == -1) {
                return -1;
            }

            // ------------------------------------------------
            // Step 2 - Create User
            // ------------------------------------------------

            String salt =
                    PasswordUtils.generateSalt();

            String passwordHash =
                    PasswordUtils.hashPassword(
                            password,
                            salt
                    );

            ContentValues userValues =
                    new ContentValues();

            userValues.put(
                    "name",
                    name
            );

            userValues.put(
                    "email",
                    email
            );

            userValues.put(
                    "password_hash",
                    passwordHash
            );

            userValues.put(
                    "password_salt",
                    salt
            );

            userValues.put(
                    "role",
                    role
            );

            userValues.put(
                    "organization_id",
                    organizationId
            );

            // Save user's selected registration location
            userValues.put(
                    "latitude",
                    latitude
            );

            userValues.put(
                    "longitude",
                    longitude
            );

            long userId =
                    database.insert(
                            "users",
                            null,
                            userValues
                    );

            // ------------------------------------------------
            // User Creation Failed
            // ------------------------------------------------

            if (userId == -1) {

                /*
                 * This can happen, for example,
                 * when the email already exists.
                 *
                 * Because we are inside a transaction,
                 * the organization created above
                 * will also be rolled back.
                 */

                return -1;
            }

            // ------------------------------------------------
            // Commit Registration Transaction
            // ------------------------------------------------

            database.setTransactionSuccessful();

            return userId;

        } finally {

            database.endTransaction();
        }
    }

    // ====================================================
    // CHANGE USER PASSWORD
    // ====================================================

    public boolean changeUserPassword(
            String email,
            String currentPassword,
            String newPassword
    ) {

        Cursor cursor = null;

        database.beginTransaction();

        try {

            // ------------------------------------------------
            // Get Current Password Data
            // ------------------------------------------------

            String query =
                    "SELECT password_hash, password_salt " +
                            "FROM users " +
                            "WHERE email = ? " +
                            "LIMIT 1";

            cursor =
                    database.rawQuery(
                            query,
                            new String[]{
                                    email
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String currentHash =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "password_hash"
                            )
                    );

            String currentSalt =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "password_salt"
                            )
                    );

            // ------------------------------------------------
            // Verify Current Password
            // ------------------------------------------------

            String enteredCurrentHash =
                    PasswordUtils.hashPassword(
                            currentPassword,
                            currentSalt
                    );

            if (!currentHash.equals(
                    enteredCurrentHash
            )) {

                return false;
            }

            // ------------------------------------------------
            // Generate New Salt + Hash
            // ------------------------------------------------

            String newSalt =
                    PasswordUtils.generateSalt();

            String newHash =
                    PasswordUtils.hashPassword(
                            newPassword,
                            newSalt
                    );

            // ------------------------------------------------
            // Update Password
            // ------------------------------------------------

            ContentValues values =
                    new ContentValues();

            values.put(
                    "password_hash",
                    newHash
            );

            values.put(
                    "password_salt",
                    newSalt
            );

            int rowsUpdated =
                    database.update(
                            "users",
                            values,
                            "email = ?",
                            new String[]{
                                    email
                            }
                    );

            if (rowsUpdated <= 0) {

                return false;
            }

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    // ====================================================
// GET FOOD ORGANIZATION DETAILS
// ====================================================

    public String[] getFoodOrganizationDetails(
            int organizationId
    ) {

        String query =
                "SELECT name, phone, address, latitude, longitude " +
                        "FROM food_organizations " +
                        "WHERE id = ? " +
                        "LIMIT 1";

        Cursor cursor =
                database.rawQuery(
                        query,
                        new String[]{
                                String.valueOf(
                                        organizationId
                                )
                        }
                );

        if (cursor.moveToFirst()) {

            String name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "name"
                            )
                    );

            String phone =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "phone"
                            )
                    );

            String address =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "address"
                            )
                    );

            double latitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "latitude"
                            )
                    );

            double longitude =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "longitude"
                            )
                    );

            cursor.close();

            return new String[]{
                    name,
                    phone,
                    address,
                    String.valueOf(latitude),
                    String.valueOf(longitude)
            };
        }

        cursor.close();

        return null;
    }

    // ====================================================
// UPDATE FOOD PROFILE
// ====================================================

    public boolean updateFoodProfile(
            int userId,
            int organizationId,
            String name,
            String phone
    ) {

        database.beginTransaction();

        Cursor cursor = null;

        try {

            // ------------------------------------------------
            // Verify User
            // ------------------------------------------------

            String query =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ?";

            cursor =
                    database.rawQuery(
                            query,
                            new String[]{
                                    String.valueOf(
                                            userId
                                    )
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String role =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            cursor.close();
            cursor = null;

            // ------------------------------------------------
            // Validate Role
            // ------------------------------------------------

            if (!"Food Institution".equals(
                    role
            )) {

                return false;
            }

            // ------------------------------------------------
            // Validate Ownership
            // ------------------------------------------------

            if (userOrganizationId
                    != organizationId) {

                return false;
            }

            // ------------------------------------------------
            // Update Organization
            // ------------------------------------------------

            ContentValues organizationValues =
                    new ContentValues();

            organizationValues.put(
                    "name",
                    name
            );

            organizationValues.put(
                    "phone",
                    phone
            );

            int organizationRows =
                    database.update(
                            "food_organizations",
                            organizationValues,
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Synchronize User Name
            // ------------------------------------------------

            ContentValues userValues =
                    new ContentValues();

            userValues.put(
                    "name",
                    name
            );

            int userRows =
                    database.update(
                            "users",
                            userValues,
                            "id = ? " +
                                    "AND organization_id = ?",
                            new String[]{
                                    String.valueOf(
                                            userId
                                    ),
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (userRows <= 0) {

                return false;
            }

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    // ====================================================
// UPDATE FOOD ORGANIZATION LOCATION
// ====================================================

    public boolean updateFoodOrganizationLocation(
            int userId,
            int organizationId,
            double latitude,
            double longitude
    ) {

        database.beginTransaction();

        Cursor cursor = null;

        try {

            String query =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ?";

            cursor =
                    database.rawQuery(
                            query,
                            new String[]{
                                    String.valueOf(
                                            userId
                                    )
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String role =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            cursor.close();
            cursor = null;

            if (!"Food Institution".equals(
                    role
            )) {

                return false;
            }

            if (userOrganizationId
                    != organizationId) {

                return false;
            }

            // ------------------------------------------------
            // Update Organization Location
            // ------------------------------------------------

            ContentValues organizationValues =
                    new ContentValues();

            organizationValues.put(
                    "latitude",
                    latitude
            );

            organizationValues.put(
                    "longitude",
                    longitude
            );

            int organizationRows =
                    database.update(
                            "food_organizations",
                            organizationValues,
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Update User Location
            // ------------------------------------------------

            ContentValues userValues =
                    new ContentValues();

            userValues.put(
                    "latitude",
                    latitude
            );

            userValues.put(
                    "longitude",
                    longitude
            );

            int userRows =
                    database.update(
                            "users",
                            userValues,
                            "id = ? " +
                                    "AND organization_id = ?",
                            new String[]{
                                    String.valueOf(
                                            userId
                                    ),
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (userRows <= 0) {

                return false;
            }

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    // ====================================================
// DELETE FOOD ORGANIZATION ACCOUNT
// ====================================================

    public java.util.List<String> deleteFoodOrganizationAccount(
            int userId,
            int organizationId
    ) {

        java.util.List<String> imagePaths =
                new java.util.ArrayList<>();

        database.beginTransaction();

        Cursor imageCursor = null;
        Cursor userCursor = null;

        try {

            // ------------------------------------------------
            // Validate User
            // ------------------------------------------------

            String userQuery =
                    "SELECT role, organization_id " +
                            "FROM users " +
                            "WHERE id = ? " +
                            "LIMIT 1";

            userCursor =
                    database.rawQuery(
                            userQuery,
                            new String[]{
                                    String.valueOf(userId)
                            }
                    );

            if (!userCursor.moveToFirst()) {
                return null;
            }

            String role =
                    userCursor.getString(
                            userCursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int storedOrganizationId =
                    userCursor.getInt(
                            userCursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            // ------------------------------------------------
            // Validate Role
            // ------------------------------------------------

            if (!"Food Institution".equals(role)) {
                return null;
            }

            // ------------------------------------------------
            // Validate Ownership
            // ------------------------------------------------

            if (storedOrganizationId
                    != organizationId) {

                return null;
            }

            userCursor.close();
            userCursor = null;

            // ------------------------------------------------
            // Get Image Paths Before Deleting Donations
            // ------------------------------------------------

            String imageQuery =
                    "SELECT image_path " +
                            "FROM food_donations " +
                            "WHERE food_organization_id = ?";

            imageCursor =
                    database.rawQuery(
                            imageQuery,
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            while (imageCursor.moveToNext()) {

                int columnIndex =
                        imageCursor.getColumnIndexOrThrow(
                                "image_path"
                        );

                if (!imageCursor.isNull(
                        columnIndex
                )) {

                    String imagePath =
                            imageCursor.getString(
                                    columnIndex
                            );

                    if (imagePath != null
                            && !imagePath.trim().isEmpty()) {

                        imagePaths.add(
                                imagePath
                        );
                    }
                }
            }

            imageCursor.close();
            imageCursor = null;

            // ------------------------------------------------
            // Delete Related Donation Requests
            // ------------------------------------------------

            database.delete(
                    "donation_requests",
                    "donation_id IN (" +
                            "SELECT id " +
                            "FROM food_donations " +
                            "WHERE food_organization_id = ?" +
                            ")",
                    new String[]{
                            String.valueOf(
                                    organizationId
                            )
                    }
            );

            // ------------------------------------------------
            // Delete Food Donations
            // ------------------------------------------------

            database.delete(
                    "food_donations",
                    "food_organization_id = ?",
                    new String[]{
                            String.valueOf(
                                    organizationId
                            )
                    }
            );

            // ------------------------------------------------
            // Delete Food Organization
            // ------------------------------------------------

            int organizationRows =
                    database.delete(
                            "food_organizations",
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {
                return null;
            }

            // ------------------------------------------------
            // Delete User
            // ------------------------------------------------

            int userRows =
                    database.delete(
                            "users",
                            "id = ? " +
                                    "AND organization_id = ? " +
                                    "AND role = ?",
                            new String[]{
                                    String.valueOf(userId),
                                    String.valueOf(
                                            organizationId
                                    ),
                                    "Food Institution"
                            }
                    );

            if (userRows <= 0) {
                return null;
            }

            // ------------------------------------------------
            // Commit Transaction
            // ------------------------------------------------

            database.setTransactionSuccessful();

            return imagePaths;

        } finally {

            if (imageCursor != null) {
                imageCursor.close();
            }

            if (userCursor != null) {
                userCursor.close();
            }

            database.endTransaction();
        }
    }

    // ====================================================
// UPDATE REQUEST QUANTITY
// ====================================================

    public int updateRequestQuantity(
            int requestId,
            int charityOrganizationId,
            int newQuantity
    ) {

        if (requestId <= 0
                || charityOrganizationId <= 0
                || newQuantity <= 0) {

            return 0;
        }

        String validationQuery =
                "SELECT dr.id " +
                        "FROM donation_requests dr " +
                        "INNER JOIN food_donations fd " +
                        "ON dr.donation_id = fd.id " +
                        "WHERE dr.id = ? " +
                        "AND dr.charity_organization_id = ? " +
                        "AND dr.status = 'PENDING' " +
                        "AND fd.status = 'AVAILABLE' " +
                        "AND fd.expiry_date >= date('now', 'localtime') " +
                        "AND ? <= fd.quantity";

        Cursor cursor =
                database.rawQuery(
                        validationQuery,
                        new String[]{
                                String.valueOf(
                                        requestId
                                ),
                                String.valueOf(
                                        charityOrganizationId
                                ),
                                String.valueOf(
                                        newQuantity
                                )
                        }
                );

        boolean valid =
                cursor.moveToFirst();

        cursor.close();

        if (!valid) {

            return 0;
        }

        ContentValues values =
                new ContentValues();

        values.put(
                "quantity_requested",
                newQuantity
        );

        return database.update(
                "donation_requests",
                values,
                "id = ? " +
                        "AND charity_organization_id = ? " +
                        "AND status = 'PENDING'",
                new String[]{
                        String.valueOf(
                                requestId
                        ),
                        String.valueOf(
                                charityOrganizationId
                        )
                }
        );
    }
    // ====================================================
    // DELETE CHARITY ACCOUNT
    // ====================================================

    public boolean deleteCharityAccount(
            int userId,
            int organizationId,
            String email,
            String currentPassword
    ) {

        database.beginTransaction();

        Cursor cursor = null;

        try {

            // ------------------------------------------------
            // Get Current User
            // ------------------------------------------------

            String query =
                    "SELECT role, organization_id, " +
                            "password_hash, password_salt " +
                            "FROM users " +
                            "WHERE id = ? " +
                            "AND email = ? " +
                            "LIMIT 1";

            cursor =
                    database.rawQuery(
                            query,
                            new String[]{
                                    String.valueOf(userId),
                                    email
                            }
                    );

            if (!cursor.moveToFirst()) {

                return false;
            }

            String role =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "role"
                            )
                    );

            int userOrganizationId =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "organization_id"
                            )
                    );

            String storedHash =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "password_hash"
                            )
                    );

            String storedSalt =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "password_salt"
                            )
                    );

            cursor.close();
            cursor = null;

            // ------------------------------------------------
            // Validate Charity Role
            // ------------------------------------------------

            if (!"Charity Organization".equals(
                    role
            )) {

                return false;
            }

            // ------------------------------------------------
            // Validate Organization Ownership
            // ------------------------------------------------

            if (userOrganizationId
                    != organizationId) {

                return false;
            }

            // ------------------------------------------------
            // Verify Current Password
            // ------------------------------------------------

            String enteredHash =
                    PasswordUtils.hashPassword(
                            currentPassword,
                            storedSalt
                    );

            if (!storedHash.equals(
                    enteredHash
            )) {

                return false;
            }

            // ------------------------------------------------
            // Delete Related Requests
            // ------------------------------------------------

            database.delete(
                    "donation_requests",
                    "charity_organization_id = ?",
                    new String[]{
                            String.valueOf(
                                    organizationId
                            )
                    }
            );

            // ------------------------------------------------
            // Delete User
            // ------------------------------------------------

            int userRows =
                    database.delete(
                            "users",
                            "id = ? " +
                                    "AND email = ? " +
                                    "AND organization_id = ?",
                            new String[]{
                                    String.valueOf(userId),
                                    email,
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (userRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Delete Charity Organization
            // ------------------------------------------------

            int organizationRows =
                    database.delete(
                            "charity_organizations",
                            "id = ?",
                            new String[]{
                                    String.valueOf(
                                            organizationId
                                    )
                            }
                    );

            if (organizationRows <= 0) {

                return false;
            }

            // ------------------------------------------------
            // Commit
            // ------------------------------------------------

            database.setTransactionSuccessful();

            return true;

        } finally {

            if (cursor != null) {

                cursor.close();
            }

            database.endTransaction();
        }
    }

    public void close() {
        databaseHelper.close();
    }
}