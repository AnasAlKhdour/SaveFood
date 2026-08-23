package com.example.savefoodapp.database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.security.PasswordUtils;
import com.example.savefoodapp.models.DonationRequest;

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

        return database.insert("users", null, values);
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
                organizationId = cursor.getInt(organizationColumnIndex);
            }

            double latitude = 0.0;
            double longitude = 0.0;

            int latitudeIndex =
                    cursor.getColumnIndexOrThrow("latitude");

            if (!cursor.isNull(latitudeIndex)) {
                latitude = cursor.getDouble(latitudeIndex);
            }

            int longitudeIndex =
                    cursor.getColumnIndexOrThrow("longitude");

            if (!cursor.isNull(longitudeIndex)) {
                longitude = cursor.getDouble(longitudeIndex);
            }

            User user = new User(
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

        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("role", user.getRole());

        if (user.getOrganizationId() > 0) {
            values.put("organization_id", user.getOrganizationId());
        } else {
            values.putNull("organization_id");
        }

        return database.update(
                "users",
                values,
                "id = ?",
                new String[]{String.valueOf(user.getId())}
        );
    }

    // T2.15 - Update User Location
    public int updateUserLocation(
            int userId,
            double latitude,
            double longitude
    ) {

        ContentValues values = new ContentValues();

        values.put("latitude", latitude);
        values.put("longitude", longitude);

        return database.update(
                "users",
                values,
                "id = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // T3.3 - Insert Food Donation
    public long insertFoodDonation(FoodDonation donation) {

        ContentValues values = new ContentValues();

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
    public java.util.List<FoodDonation> getFoodDonationsByOrganizationId(
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

        Cursor cursor = database.rawQuery(
                query,
                new String[]{String.valueOf(organizationId)}
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            int foodOrganizationId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("food_organization_id")
            );

            String foodName = cursor.getString(
                    cursor.getColumnIndexOrThrow("food_name")
            );

            int quantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiry_date")
            );

            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
            );

            String imagePath = cursor.getString(
                    cursor.getColumnIndexOrThrow("image_path")
            );

            FoodDonation donation = new FoodDonation(
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
    public FoodDonation getFoodDonationById(int donationId) {

        String query =
                "SELECT id, food_organization_id, food_name, quantity, " +
                        "description, expiry_date, status, image_path " +
                        "FROM food_donations " +
                        "WHERE id = ?";

        Cursor cursor = database.rawQuery(
                query,
                new String[]{String.valueOf(donationId)}
        );

        if (cursor.moveToFirst()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            int foodOrganizationId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("food_organization_id")
            );

            String foodName = cursor.getString(
                    cursor.getColumnIndexOrThrow("food_name")
            );

            int quantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiry_date")
            );

            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
            );

            String imagePath = cursor.getString(
                    cursor.getColumnIndexOrThrow("image_path")
            );

            FoodDonation donation = new FoodDonation(
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

    // T3.5 - Update Food Donation
    public int updateFoodDonation(
            int donationId,
            String foodName,
            int quantity,
            String description,
            String expiryDate
    ) {

        ContentValues values = new ContentValues();

        values.put("food_name", foodName);
        values.put("quantity", quantity);
        values.put("description", description);
        values.put("expiry_date", expiryDate);

        return database.update(
                "food_donations",
                values,
                "id = ?",
                new String[]{String.valueOf(donationId)}
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

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("address", address);
        values.put("latitude", latitude);
        values.put("longitude", longitude);

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

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("address", address);
        values.put("latitude", latitude);
        values.put("longitude", longitude);

        return database.insert(
                "charity_organizations",
                null,
                values
        );
    }

    // T3.3 - Link user to organization
    public int updateUserOrganizationId(
            String email,
            int organizationId
    ) {

        ContentValues values = new ContentValues();

        values.put("organization_id", organizationId);

        return database.update(
                "users",
                values,
                "email = ?",
                new String[]{email}
        );
    }

    // T3.6 - Delete Food Donation
    public int deleteFoodDonation(int donationId) {

        database.beginTransaction();

        try {

            // Delete requests related to this donation first
            database.delete(
                    "donation_requests",
                    "donation_id = ?",
                    new String[]{
                            String.valueOf(donationId)
                    }
            );

            // Delete the donation
            int result =
                    database.delete(
                            "food_donations",
                            "id = ?",
                            new String[]{
                                    String.valueOf(donationId)
                            }
                    );

            database.setTransactionSuccessful();

            return result;

        } finally {

            database.endTransaction();
        }
    }

    // T5.2 - Get Available Offers
    public java.util.List<FoodDonation> getAvailableOffers() {

        java.util.List<FoodDonation> offers =
                new java.util.ArrayList<>();

        String query =
                "SELECT id, food_organization_id, food_name, quantity, " +
                        "description, expiry_date, status, image_path " +
                        "FROM food_donations " +
                        "WHERE status = ? " +
                        "ORDER BY id DESC";

        Cursor cursor = database.rawQuery(
                query,
                new String[]{"AVAILABLE"}
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            int foodOrganizationId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("food_organization_id")
            );

            String foodName = cursor.getString(
                    cursor.getColumnIndexOrThrow("food_name")
            );

            int quantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiry_date")
            );

            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
            );

            String imagePath = cursor.getString(
                    cursor.getColumnIndexOrThrow("image_path")
            );

            FoodDonation donation = new FoodDonation(
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
    public double[] getOrganizationLocation(int organizationId) {

        String query =
                "SELECT latitude, longitude " +
                        "FROM food_organizations " +
                        "WHERE id = ? " +
                        "AND latitude IS NOT NULL " +
                        "AND longitude IS NOT NULL " +
                        "LIMIT 1";

        Cursor cursor = database.rawQuery(
                query,
                new String[]{String.valueOf(organizationId)}
        );

        if (cursor.moveToFirst()) {

            double latitude = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("latitude")
            );

            double longitude = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("longitude")
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
    public long insertRequest(DonationRequest request) {

        ContentValues values = new ContentValues();

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
                request.getStatus()
        );

        return database.insert(
                "donation_requests",
                null,
                values
        );
    }

    // T6.11 - Get Requests by Charity
    public java.util.List<DonationRequest> getRequestsByCharity(
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

        Cursor cursor = database.rawQuery(
                query,
                new String[]{
                        String.valueOf(charityOrganizationId)
                }
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            int donationId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("donation_id")
            );

            int charityId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "charity_organization_id"
                    )
            );

            int quantityRequested = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "quantity_requested"
                    )
            );

            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
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

    // T6.12 - Get Requests by Food Institution
    public java.util.List<DonationRequest> getRequestsByInstitution(
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

        Cursor cursor = database.rawQuery(
                query,
                new String[]{
                        String.valueOf(foodOrganizationId)
                }
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            int donationId = cursor.getInt(
                    cursor.getColumnIndexOrThrow("donation_id")
            );

            int charityId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "charity_organization_id"
                    )
            );

            int quantityRequested = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "quantity_requested"
                    )
            );

            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow("status")
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
            String status
    ) {

        ContentValues values = new ContentValues();

        values.put(
                "status",
                status
        );

        return database.update(
                "donation_requests",
                values,
                "id = ?",
                new String[]{
                        String.valueOf(requestId)
                }
        );
    }

    // T6.8 - Accept Request and Update Donation Quantity
    public boolean acceptRequest(int requestId) {

        database.beginTransaction();

        try {

            String requestQuery =
                    "SELECT donation_id, quantity_requested, status " +
                            "FROM donation_requests " +
                            "WHERE id = ?";

            Cursor requestCursor =
                    database.rawQuery(
                            requestQuery,
                            new String[]{
                                    String.valueOf(requestId)
                            }
                    );

            if (!requestCursor.moveToFirst()) {

                requestCursor.close();
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

            requestCursor.close();

            if (!"PENDING".equalsIgnoreCase(requestStatus)) {
                return false;
            }

            String donationQuery =
                    "SELECT quantity " +
                            "FROM food_donations " +
                            "WHERE id = ?";

            Cursor donationCursor =
                    database.rawQuery(
                            donationQuery,
                            new String[]{
                                    String.valueOf(donationId)
                            }
                    );

            if (!donationCursor.moveToFirst()) {

                donationCursor.close();
                return false;
            }

            int currentQuantity =
                    donationCursor.getInt(
                            donationCursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            donationCursor.close();

            if (quantityRequested > currentQuantity) {
                return false;
            }

            int remainingQuantity =
                    currentQuantity - quantityRequested;

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

            int donationUpdated =
                    database.update(
                            "food_donations",
                            donationValues,
                            "id = ?",
                            new String[]{
                                    String.valueOf(donationId)
                            }
                    );

            if (donationUpdated <= 0) {
                return false;
            }

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
                            "id = ?",
                            new String[]{
                                    String.valueOf(requestId)
                            }
                    );

            if (requestUpdated <= 0) {
                return false;
            }

            database.setTransactionSuccessful();

            return true;

        } finally {

            database.endTransaction();
        }
    }

    public void close() {
        databaseHelper.close();
    }
}