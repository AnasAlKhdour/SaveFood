package com.example.savefoodapp.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.example.savefoodapp.core.image.ImageUtils;
import com.example.savefoodapp.data.database.DBAdapter;
import com.example.savefoodapp.data.models.FoodDonation;

import java.util.List;

public class FoodDonationRepository {

    private final DBAdapter dbAdapter;

    public FoodDonationRepository(
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
    // INSERT FOOD DONATION
    // ====================================================

    /**
     * Insert a new food donation.
     *
     * @return inserted donation ID,
     *         or -1 if insertion failed
     */
    public long insertFoodDonation(
            FoodDonation donation
    ) {

        return dbAdapter.insertFoodDonation(
                donation
        );
    }

    // ====================================================
    // GET FOOD DONATIONS
    // ====================================================

    /**
     * Get all food donations belonging
     * to a specific food organization.
     */
    public List<FoodDonation>
    getFoodDonationsByOrganizationId(
            int organizationId
    ) {

        return dbAdapter.getFoodDonationsByOrganizationId(
                organizationId
        );
    }

    // ====================================================
    // GET FOOD DONATION BY ID
    // ====================================================

    /**
     * Get a food donation by its ID.
     *
     * @return FoodDonation object,
     *         or null if not found
     */
    public FoodDonation getFoodDonationById(
            int donationId
    ) {

        return dbAdapter.getFoodDonationById(
                donationId
        );
    }

    // ====================================================
    // UPDATE FOOD DONATION
    // ====================================================

    /**
     * Update food donation information.
     *
     * imagePath is optional:
     *
     * - null / empty:
     *   keep the existing image.
     *
     * - non-empty:
     *   save the new image path.
     *
     * The old image is deleted only after
     * a successful database update.
     *
     * @return number of updated rows
     */
    public int updateFoodDonation(
            int donationId,
            int foodOrganizationId,
            String foodName,
            int quantity,
            String description,
            String expiryDate,
            String imagePath
    ) {

        // ------------------------------------------------
        // Get Existing Donation
        // ------------------------------------------------

        FoodDonation existingDonation =
                dbAdapter.getFoodDonationById(
                        donationId
                );

        if (existingDonation == null) {

            return 0;
        }

        // ------------------------------------------------
        // Existing Image
        // ------------------------------------------------

        String oldImagePath =
                existingDonation.getImagePath();

        // ------------------------------------------------
        // Decide Image Path
        // ------------------------------------------------

        String imagePathToSave;

        if (TextUtils.isEmpty(
                imagePath
        )) {

            // No new image selected.
            // Keep current image.

            imagePathToSave =
                    oldImagePath;

        } else {

            // New image selected.

            imagePathToSave =
                    imagePath;
        }

        // ------------------------------------------------
        // Update Database
        // ------------------------------------------------

        int result =
                dbAdapter.updateFoodDonation(
                        donationId,
                        foodOrganizationId,
                        foodName,
                        quantity,
                        description,
                        expiryDate,
                        imagePathToSave
                );

        // ------------------------------------------------
        // Delete Old Image
        // Only After Successful Update
        // ------------------------------------------------

        if (result > 0
                && !TextUtils.isEmpty(
                imagePath
        )
                && !TextUtils.isEmpty(
                oldImagePath
        )
                && !oldImagePath.equals(
                imagePath
        )) {

            ImageUtils.deleteImage(
                    oldImagePath
            );
        }

        return result;
    }

    // ====================================================
    // DELETE FOOD DONATION
    // ====================================================

    /**
     * Delete a food donation.
     *
     * Related donation requests are deleted
     * by DBAdapter inside a transaction.
     *
     * After successful deletion, the associated
     * image file is also removed from device storage.
     *
     * @return number of deleted donations
     */
    public int deleteFoodDonation(
            int donationId,
            int foodOrganizationId
    ) {

        // ------------------------------------------------
        // Get Existing Donation
        // ------------------------------------------------

        FoodDonation donation =
                dbAdapter.getFoodDonationById(
                        donationId
                );

        String imagePath = null;

        if (donation != null) {

            imagePath =
                    donation.getImagePath();
        }

        // ------------------------------------------------
        // Delete Donation
        // ------------------------------------------------

        int result =
                dbAdapter.deleteFoodDonation(
                        donationId,
                        foodOrganizationId
                );

        // ------------------------------------------------
        // Delete Image Only After
        // Successful Database Deletion
        // ------------------------------------------------

        if (result > 0) {

            ImageUtils.deleteImage(
                    imagePath
            );
        }

        return result;
    }

    // ====================================================
    // GET AVAILABLE OFFERS
    // ====================================================

    /**
     * Get all currently available food offers.
     */
    public List<FoodDonation>
    getAvailableOffers() {

        return dbAdapter.getAvailableOffers();
    }

    // ====================================================
    // ORGANIZATION LOCATION
    // ====================================================

    /**
     * Get the location of a food organization.
     *
     * @return double array:
     * [0] = latitude
     * [1] = longitude
     *
     * Returns null if location is not available.
     */
    public double[] getOrganizationLocation(
            int organizationId
    ) {

        return dbAdapter.getOrganizationLocation(
                organizationId
        );
    }

    // ====================================================
    // ACCEPT REQUEST
    // ====================================================

    /**
     * Accept a donation request.
     *
     * This operation updates both:
     * - donation quantity/status
     * - request status
     *
     * DBAdapter handles the transaction.
     */
    public boolean acceptRequest(
            int requestId,
            int foodOrganizationId
    ) {

        return dbAdapter.acceptRequest(
                requestId,
                foodOrganizationId
        );
    }
}