package com.example.savefoodapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

public class AddOfferActivity extends AppCompatActivity {

    private EditText etFoodName;
    private EditText etQuantity;
    private EditText etDescription;
    private EditText etExpiryDate;

    private Button btnCreateOffer;
    private Button btnCancel;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_offer);

        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etDescription = findViewById(R.id.etDescription);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        btnCreateOffer = findViewById(R.id.btnCreateOffer);
        btnCancel = findViewById(R.id.btnCancel);

        dbAdapter = new DBAdapter(this);
        sessionManager = new SessionManager(this);

        btnCreateOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOffer();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void createOffer() {

        String foodName = etFoodName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Food name validation
        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Please enter food name");
            etFoodName.requestFocus();
            return;
        }

        // Quantity validation
        if (TextUtils.isEmpty(quantityText)) {
            etQuantity.setError("Please enter quantity");
            etQuantity.requestFocus();
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid quantity");
            etQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }

        // Expiry date validation
        if (TextUtils.isEmpty(expiryDate)) {
            etExpiryDate.setError("Please enter expiry date");
            etExpiryDate.requestFocus();
            return;
        }

        // Get logged-in user's email
        String userEmail = sessionManager.getUserEmail();

        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(
                    this,
                    "User session not found",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Get current user from database
        dbAdapter.open();

        User user = dbAdapter.getUser(userEmail);

        if (user == null) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    "User not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int organizationId = user.getOrganizationId();

        if (organizationId <= 0) {

            long newOrgId = dbAdapter.insertFoodOrganization(
                    user.getName(),
                    "-",
                    "-"
            );

            if (newOrgId == -1) {

                dbAdapter.close();

                Toast.makeText(
                        this,
                        "Failed to create food organization",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            int updated = dbAdapter.updateUserOrganizationId(
                    userEmail,
                    (int) newOrgId
            );

            if (updated == 0) {

                dbAdapter.close();

                Toast.makeText(
                        this,
                        "Failed to link organization",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            organizationId = (int) newOrgId;
        }
        // Create FoodDonation object
        FoodDonation donation = new FoodDonation(
                0,
                organizationId,
                foodName,
                quantity,
                description,
                expiryDate,
                "AVAILABLE"
        );

        // Insert donation
        long donationId = dbAdapter.insertFoodDonation(donation);

        dbAdapter.close();

        if (donationId == -1) {

            Toast.makeText(
                    this,
                    "Failed to create offer",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Offer created successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }
}