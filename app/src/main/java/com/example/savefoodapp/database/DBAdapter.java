package com.example.savefoodapp.database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.savefoodapp.models.User;
import com.example.savefoodapp.security.PasswordUtils;

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

            // Get user's location
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

            // Create User object
            User user = new User(
                    id,
                    name,
                    userEmail,
                    passwordHash,
                    passwordSalt,
                    role,
                    organizationId
            );

            // Set location
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

    public void close() {
        databaseHelper.close();
    }
}