package com.example.savefoodapp.data.repository;

import android.content.Context;

import com.example.savefoodapp.data.database.DBAdapter;

import java.util.List;

public class OrganizationRepository {

    private final DBAdapter dbAdapter;

    public OrganizationRepository(
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
    // INSERT FOOD ORGANIZATION
    // ====================================================

    /**
     * Insert a Food Organization.
     */
    public long insertFoodOrganization(
            String name,
            String phone,
            String address,
            double latitude,
            double longitude
    ) {

        return dbAdapter.insertFoodOrganization(
                name,
                phone,
                address,
                latitude,
                longitude
        );
    }

    // ====================================================
    // INSERT CHARITY ORGANIZATION
    // ====================================================

    /**
     * Insert a Charity Organization.
     */
    public long insertCharityOrganization(
            String name,
            String phone,
            String address,
            double latitude,
            double longitude
    ) {

        return dbAdapter.insertCharityOrganization(
                name,
                phone,
                address,
                latitude,
                longitude
        );
    }

    // ====================================================
    // GET FOOD ORGANIZATION LOCATION
    // ====================================================

    /**
     * Get Food Organization location.
     *
     * @return double array:
     * [0] = latitude
     * [1] = longitude
     */
    public double[] getFoodOrganizationLocation(
            int organizationId
    ) {

        return dbAdapter.getOrganizationLocation(
                organizationId
        );
    }

    // ====================================================
    // REGISTER ORGANIZATION + USER
    // ====================================================

    /**
     * Complete registration transaction.
     *
     * Creates:
     * - Organization
     * - User
     * - User location
     *
     * All operations are performed inside
     * one database transaction.
     *
     * @return created user ID,
     *         or -1 if registration failed
     */
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

        return dbAdapter.registerOrganizationAndUser(
                organizationType,
                name,
                phone,
                address,
                latitude,
                longitude,
                email,
                password,
                role
        );
    }

    // ====================================================
    // CHARITY ORGANIZATION DETAILS
    // ====================================================

    /**
     * Get Charity Organization details.
     *
     * @return
     * [name, phone, address, latitude, longitude]
     * or null if organization was not found.
     */
    public String[] getCharityOrganizationDetails(
            int organizationId
    ) {

        return dbAdapter.getCharityOrganizationDetails(
                organizationId
        );
    }

    // ====================================================
    // UPDATE CHARITY PROFILE
    // ====================================================

    /**
     * Update Charity Organization profile.
     *
     * Updates:
     * - Organization name
     * - Organization phone
     * - Linked user name
     *
     * Email, role and organization ID remain unchanged.
     */
    public boolean updateCharityProfile(
            int userId,
            int organizationId,
            String name,
            String phone
    ) {

        return dbAdapter.updateCharityProfile(
                userId,
                organizationId,
                name,
                phone
        );
    }

    // ====================================================
    // UPDATE CHARITY LOCATION
    // ====================================================

    /**
     * Update Charity Organization location.
     *
     * Updates both:
     * - charity_organizations
     * - users
     *
     * inside one transaction.
     */
    public boolean updateCharityOrganizationLocation(
            int userId,
            int organizationId,
            double latitude,
            double longitude
    ) {

        return dbAdapter.updateCharityOrganizationLocation(
                userId,
                organizationId,
                latitude,
                longitude
        );
    }

    // ====================================================
    // DELETE CHARITY ACCOUNT
    // ====================================================

    /**
     * Delete a complete Charity account.
     *
     * Deletes:
     * - Donation requests owned by the charity
     * - User account
     * - Charity organization
     *
     * All operations are performed inside one transaction.
     */
    public boolean deleteCharityAccount(
            int userId,
            int organizationId,
            String email,
            String currentPassword
    ) {

        return dbAdapter.deleteCharityAccount(
                userId,
                organizationId,
                email,
                currentPassword
        );
    }

    // ====================================================
    // FOOD ORGANIZATION DETAILS
    // ====================================================

    /**
     * Get Food Organization details.
     */
    public String[] getFoodOrganizationDetails(
            int organizationId
    ) {

        return dbAdapter.getFoodOrganizationDetails(
                organizationId
        );
    }

    // ====================================================
    // UPDATE FOOD PROFILE
    // ====================================================

    /**
     * Update Food Organization profile.
     */
    public boolean updateFoodProfile(
            int userId,
            int organizationId,
            String name,
            String phone
    ) {

        return dbAdapter.updateFoodProfile(
                userId,
                organizationId,
                name,
                phone
        );
    }

    // ====================================================
    // UPDATE FOOD LOCATION
    // ====================================================

    /**
     * Update Food Organization location.
     *
     * Updates both:
     * - food_organizations
     * - users
     *
     * inside one transaction.
     */
    public boolean updateFoodOrganizationLocation(
            int userId,
            int organizationId,
            double latitude,
            double longitude
    ) {

        return dbAdapter.updateFoodOrganizationLocation(
                userId,
                organizationId,
                latitude,
                longitude
        );
    }

    // ====================================================
    // DELETE FOOD ORGANIZATION ACCOUNT
    // ====================================================

    /**
     * Delete a complete Food Organization account.
     *
     * @return image paths that should be removed
     *         from device storage, or null on failure.
     */
    public List<String> deleteFoodOrganizationAccount(
            int userId,
            int organizationId
    ) {

        return dbAdapter.deleteFoodOrganizationAccount(
                userId,
                organizationId
        );
    }
}