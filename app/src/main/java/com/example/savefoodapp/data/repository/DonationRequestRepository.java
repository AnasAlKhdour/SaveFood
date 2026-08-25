package com.example.savefoodapp.data.repository;

import android.content.Context;

import com.example.savefoodapp.data.database.DBAdapter;
import com.example.savefoodapp.data.models.DonationRequest;

import java.util.List;

public class DonationRequestRepository {

    private final DBAdapter dbAdapter;

    public DonationRequestRepository(
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
    // INSERT REQUEST
    // ====================================================

    /**
     * Insert a new donation request.
     *
     * The user ID allows DBAdapter to verify
     * that the user belongs to the specified
     * charity organization.
     *
     * @return inserted request ID,
     *         or -1 if insertion failed
     */
    public long insertRequest(
            DonationRequest request,
            int userId
    ) {

        return dbAdapter.insertRequest(
                request,
                userId
        );
    }

    // ====================================================
    // GET CHARITY REQUESTS
    // ====================================================

    /**
     * Get requests submitted by a specific
     * charity organization.
     */
    public List<DonationRequest>
    getRequestsByCharity(
            int charityOrganizationId
    ) {

        return dbAdapter.getRequestsByCharity(
                charityOrganizationId
        );
    }

    // ====================================================
    // GET INSTITUTION REQUESTS
    // ====================================================

    /**
     * Get requests received by a specific
     * food institution.
     */
    public List<DonationRequest>
    getRequestsByInstitution(
            int foodOrganizationId
    ) {

        return dbAdapter.getRequestsByInstitution(
                foodOrganizationId
        );
    }

    // ====================================================
    // UPDATE REQUEST STATUS
    // ====================================================

    /**
     * Update request status only if the request
     * belongs to the specified food organization.
     *
     * @return number of updated rows
     */
    public int updateRequestStatus(
            int requestId,
            int foodOrganizationId,
            String status
    ) {

        return dbAdapter.updateRequestStatus(
                requestId,
                foodOrganizationId,
                status
        );
    }

    // ====================================================
    // UPDATE REQUEST QUANTITY
    // ====================================================

    /**
     * Update requested quantity only if the request
     * belongs to the specified charity organization.
     *
     * @return true if the request was updated
     */
    public boolean updateRequestQuantity(
            int requestId,
            int charityOrganizationId,
            int newQuantity
    ) {

        return dbAdapter.updateRequestQuantity(
                requestId,
                charityOrganizationId,
                newQuantity
        ) > 0;
    }

    // ====================================================
    // CANCEL REQUEST
    // ====================================================

    /**
     * Cancel a donation request only if it belongs
     * to the specified charity organization.
     *
     * @return true if the request was cancelled
     */
    public boolean cancelRequest(
            int requestId,
            int charityOrganizationId
    ) {

        return dbAdapter.cancelRequest(
                requestId,
                charityOrganizationId
        ) > 0;
    }

    // ====================================================
    // ACCEPT REQUEST
    // ====================================================

    /**
     * Accept a donation request only if its
     * related donation belongs to the specified
     * food organization.
     *
     * DBAdapter handles the required transaction.
     *
     * @return true if the request was accepted
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