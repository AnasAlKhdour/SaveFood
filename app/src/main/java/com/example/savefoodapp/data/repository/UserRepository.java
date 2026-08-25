package com.example.savefoodapp.data.repository;

import android.content.Context;

import com.example.savefoodapp.data.database.DBAdapter;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.security.PasswordUtils;

public class UserRepository {

    private final DBAdapter dbAdapter;

    public UserRepository(
            Context context
    ) {

        dbAdapter =
                new DBAdapter(
                        context
                );
    }

    // ====================================================
    // OPEN / CLOSE
    // ====================================================

    /**
     * Open database connection.
     */
    public void open() {

        dbAdapter.open();
    }

    /**
     * Close database connection.
     */
    public void close() {

        dbAdapter.close();
    }

    // ====================================================
    // INSERT USER
    // ====================================================

    /**
     * Insert a new user.
     *
     * @return inserted user ID,
     *         or -1 if insertion failed
     */
    public long insertUser(
            User user
    ) {

        return dbAdapter.insertUser(
                user
        );
    }

    // ====================================================
    // GET USER
    // ====================================================

    /**
     * Get user by email.
     *
     * @return User object,
     *         or null if not found
     */
    public User getUser(
            String email
    ) {

        return dbAdapter.getUser(
                email
        );
    }

    // ====================================================
    // AUTHENTICATE USER
    // ====================================================

    /**
     * Authenticate a user using email and password.
     *
     * The repository:
     * 1. Loads the user.
     * 2. Reads the stored password hash and salt.
     * 3. Verifies the provided password.
     *
     * @return authenticated User,
     *         or null if credentials are invalid
     */
    public User authenticate(
            String email,
            String password
    ) {

        User user =
                dbAdapter.getUser(
                        email
                );

        if (user == null) {

            return null;
        }

        String storedHash =
                user.getPassword();

        String storedSalt =
                user.getPasswordSalt();

        boolean passwordCorrect =
                PasswordUtils.verifyPassword(
                        password,
                        storedSalt,
                        storedHash
                );

        if (!passwordCorrect) {

            return null;
        }

        return user;
    }

    // ====================================================
    // UPDATE USER
    // ====================================================

    /**
     * Update user information.
     *
     * @return number of updated rows
     */
    public int updateUser(
            User user
    ) {

        return dbAdapter.updateUser(
                user
        );
    }

    // ====================================================
    // UPDATE USER LOCATION
    // ====================================================

    /**
     * Update user's current location.
     *
     * @return number of updated rows
     */
    public int updateUserLocation(
            int userId,
            double latitude,
            double longitude
    ) {

        return dbAdapter.updateUserLocation(
                userId,
                latitude,
                longitude
        );
    }

    // ====================================================
    // UPDATE ORGANIZATION ID
    // ====================================================

    /**
     * Update the organization associated
     * with a user.
     *
     * @return number of updated rows
     */
    public int updateUserOrganizationId(
            String email,
            int organizationId
    ) {

        return dbAdapter.updateUserOrganizationId(
                email,
                organizationId
        );
    }

    // ====================================================
    // CHANGE PASSWORD
    // ====================================================

    /**
     * Change the password of a user.
     *
     * The current password must be correct.
     *
     * @return true if the password was changed
     */
    public boolean changeUserPassword(
            String email,
            String currentPassword,
            String newPassword
    ) {

        return dbAdapter.changeUserPassword(
                email,
                currentPassword,
                newPassword
        );
    }
}