package com.example.savefoodapp.data.repository;

import android.content.Context;

import com.example.savefoodapp.data.database.DBAdapter;
import com.example.savefoodapp.data.models.DonationRequest;

import java.util.List;

public class RequestRepository {

    private final DBAdapter dbAdapter;

    public RequestRepository(Context context) {
        dbAdapter = new DBAdapter(context);
    }

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

    /**
     * Insert a new donation request.
     *
     * @param request donation request
     * @param userId logged-in user ID
     * @return inserted request ID, or -1 if failed
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

    /**
     * Get requests belonging to a charity organization.
     */
    public List<DonationRequest> getRequestsByCharity(
            int charityOrganizationId
    ) {

        return dbAdapter.getRequestsByCharity(
                charityOrganizationId
        );
    }

    /**
     * Get requests received by a food institution.
     */
    public List<DonationRequest> getRequestsByInstitution(
            int foodOrganizationId
    ) {

        return dbAdapter.getRequestsByInstitution(
                foodOrganizationId
        );
    }

    /**
     * Update request status.
     *
     * The request must belong to the specified
     * food organization.
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

    /**
     * Accept a donation request.
     *
     * The request must belong to the specified
     * food organization.
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