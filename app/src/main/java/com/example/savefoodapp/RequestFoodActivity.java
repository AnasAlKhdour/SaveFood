package com.example.savefoodapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.DonationRequest;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

public class RequestFoodActivity extends AppCompatActivity {

    private TextView tvFoodName;
    private TextView tvAvailableQuantity;

    private EditText etRequestedQuantity;

    private Button btnSubmitRequest;
    private Button btnBack;

    private DBAdapter dbAdapter;

    private int donationId;
    private int availableQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_food);

        tvFoodName =
                findViewById(R.id.tvFoodName);

        tvAvailableQuantity =
                findViewById(R.id.tvAvailableQuantity);

        etRequestedQuantity =
                findViewById(R.id.etRequestedQuantity);

        btnSubmitRequest =
                findViewById(R.id.btnSubmitRequest);

        btnBack =
                findViewById(R.id.btnBack);

        // Get offer information
        donationId =
                getIntent().getIntExtra(
                        "OFFER_ID",
                        -1
                );

        String foodName =
                getIntent().getStringExtra(
                        "FOOD_NAME"
                );

        availableQuantity =
                getIntent().getIntExtra(
                        "AVAILABLE_QUANTITY",
                        0
                );

        tvFoodName.setText(
                foodName
        );

        tvAvailableQuantity.setText(
                "Available Quantity: "
                        + availableQuantity
        );

        dbAdapter =
                new DBAdapter(this);

        dbAdapter.open();

        btnBack.setOnClickListener(
                view -> finish()
        );

        btnSubmitRequest.setOnClickListener(
                view -> submitRequest()
        );
    }

    // ====================================================
    // T6.2 + T6.3 + T6.4
    // ====================================================

    private void submitRequest() {

        String quantityText =
                etRequestedQuantity
                        .getText()
                        .toString()
                        .trim();

        // Empty quantity
        if (TextUtils.isEmpty(quantityText)) {

            etRequestedQuantity.setError(
                    "Enter requested quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        int requestedQuantity;

        try {

            requestedQuantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            etRequestedQuantity.setError(
                    "Enter a valid quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // Quantity must be positive
        if (requestedQuantity <= 0) {

            etRequestedQuantity.setError(
                    "Quantity must be greater than 0"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // Cannot request more than available
        if (requestedQuantity > availableQuantity) {

            etRequestedQuantity.setError(
                    "Requested quantity cannot exceed available quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // Validate donation ID
        if (donationId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid food offer",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Get current logged-in user
        SessionManager sessionManager =
                new SessionManager(this);

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            Toast.makeText(
                    this,
                    "User session not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        User user =
                dbAdapter.getUser(email);

        if (user == null) {

            Toast.makeText(
                    this,
                    "Unable to load user information",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Make sure user is a charity
        if (!"Charity Organization".equals(
                user.getRole()
        )) {

            Toast.makeText(
                    this,
                    "Only charity organizations can request food",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            Toast.makeText(
                    this,
                    "Charity organization not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Create Request
        DonationRequest request =
                new DonationRequest(
                        0,
                        donationId,
                        charityOrganizationId,
                        requestedQuantity,
                        "PENDING"
                );

        long requestId =
                dbAdapter.insertRequest(
                        request
                );

        if (requestId == -1) {

            Toast.makeText(
                    this,
                    "Failed to create request",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Food request submitted successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (dbAdapter != null) {
            dbAdapter.close();
        }
    }
}