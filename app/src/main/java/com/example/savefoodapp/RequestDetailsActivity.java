package com.example.savefoodapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.DonationRequest;
import com.example.savefoodapp.models.FoodDonation;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView tvRequestId;
    private TextView tvFoodName;
    private TextView tvAvailableQuantity;
    private TextView tvRequestedQuantity;
    private TextView tvDescription;
    private TextView tvExpiryDate;
    private TextView tvStatus;

    private Button btnAccept;
    private Button btnReject;
    private Button btnBack;

    private DBAdapter dbAdapter;

    private int requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_details);

        // Initialize views
        tvRequestId = findViewById(R.id.tvRequestId);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvAvailableQuantity = findViewById(R.id.tvAvailableQuantity);
        tvRequestedQuantity = findViewById(R.id.tvRequestedQuantity);
        tvDescription = findViewById(R.id.tvDescription);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvStatus = findViewById(R.id.tvStatus);

        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnBack = findViewById(R.id.btnBack);

        // Initialize database
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Get Request ID
        requestId =
                getIntent().getIntExtra(
                        "REQUEST_ID",
                        -1
                );

        if (requestId == -1) {

            Toast.makeText(
                    this,
                    "Invalid request",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Load request details
        loadRequestDetails();

        // Back
        btnBack.setOnClickListener(
                view -> finish()
        );

        // Accept
        btnAccept.setOnClickListener(
                view -> acceptRequest()
        );

        // Reject
        btnReject.setOnClickListener(
                view -> rejectRequest()
        );
    }

    private void loadRequestDetails() {

        // Get request
        DonationRequest request =
                getRequestById(requestId);

        if (request == null) {

            Toast.makeText(
                    this,
                    "Request not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Get related food donation
        FoodDonation donation =
                dbAdapter.getFoodDonationById(
                        request.getDonationId()
                );

        if (donation == null) {

            Toast.makeText(
                    this,
                    "Food offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Display request details
        tvRequestId.setText(
                "Request ID: " + request.getId()
        );

        tvFoodName.setText(
                "Food: " + donation.getFoodName()
        );

        tvAvailableQuantity.setText(
                "Available Quantity: "
                        + donation.getQuantity()
        );

        tvRequestedQuantity.setText(
                "Requested Quantity: "
                        + request.getQuantityRequested()
        );

        tvDescription.setText(
                "Description: "
                        + donation.getDescription()
        );

        tvExpiryDate.setText(
                "Expiry Date: "
                        + donation.getExpiryDate()
        );

        tvStatus.setText(
                "Status: "
                        + request.getStatus()
        );

        // Enable/disable action buttons
        if ("PENDING".equalsIgnoreCase(
                request.getStatus()
        )) {

            btnAccept.setEnabled(true);
            btnReject.setEnabled(true);

        } else {

            btnAccept.setEnabled(false);
            btnReject.setEnabled(false);
        }
    }

    private DonationRequest getRequestById(
            int requestId
    ) {

        // Search inside incoming requests
        String email = null;

        com.example.savefoodapp.utils.SessionManager
                sessionManager =
                new com.example.savefoodapp.utils.SessionManager(
                        this
                );

        email =
                sessionManager.getUserEmail();

        com.example.savefoodapp.models.User user =
                dbAdapter.getUser(email);

        if (user == null) {
            return null;
        }

        int organizationId =
                user.getOrganizationId();

        java.util.List<DonationRequest> requests =
                dbAdapter.getRequestsByInstitution(
                        organizationId
                );

        for (DonationRequest request : requests) {

            if (request.getId() == requestId) {
                return request;
            }
        }

        return null;
    }

    // T6.8 - Accept Request
    private void acceptRequest() {

        boolean success =
                dbAdapter.acceptRequest(requestId);

        if (success) {

            Toast.makeText(
                    this,
                    "Request accepted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

        } else {

            Toast.makeText(
                    this,
                    "Unable to accept request. " +
                            "Requested quantity may exceed available quantity.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // T6.9 - Reject Request
    private void rejectRequest() {

        int rowsUpdated =
                dbAdapter.updateRequestStatus(
                        requestId,
                        "REJECTED"
                );

        if (rowsUpdated > 0) {

            Toast.makeText(
                    this,
                    "Request rejected successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

        } else {

            Toast.makeText(
                    this,
                    "Failed to reject request",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (dbAdapter != null) {
            dbAdapter.close();
        }
    }
}