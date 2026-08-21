package com.example.savefoodapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;

public class EditOfferActivity extends AppCompatActivity {

    private EditText etFoodName;
    private EditText etQuantity;
    private EditText etDescription;
    private EditText etExpiryDate;

    private Button btnUpdateOffer;
    private Button btnCancel;

    private DBAdapter dbAdapter;

    private int offerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_offer);

        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etDescription = findViewById(R.id.etDescription);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        btnUpdateOffer = findViewById(R.id.btnUpdateOffer);
        btnCancel = findViewById(R.id.btnCancel);

        dbAdapter = new DBAdapter(this);

        offerId = getIntent().getIntExtra("OFFER_ID", -1);

        if (offerId == -1) {
            Toast.makeText(
                    this,
                    "Offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadOffer();

        btnUpdateOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOffer();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadOffer() {

        dbAdapter.open();

        com.example.savefoodapp.models.FoodDonation offer =
                dbAdapter.getFoodDonationById(offerId);

        dbAdapter.close();

        if (offer == null) {

            Toast.makeText(
                    this,
                    "Offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        etFoodName.setText(offer.getFoodName());
        etQuantity.setText(String.valueOf(offer.getQuantity()));
        etDescription.setText(offer.getDescription());
        etExpiryDate.setText(offer.getExpiryDate());
    }

    private void updateOffer() {

        String foodName =
                etFoodName.getText().toString().trim();

        String quantityText =
                etQuantity.getText().toString().trim();

        String description =
                etDescription.getText().toString().trim();

        String expiryDate =
                etExpiryDate.getText().toString().trim();

        if (TextUtils.isEmpty(foodName)) {
            etFoodName.setError("Please enter food name");
            etFoodName.requestFocus();
            return;
        }

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

        if (TextUtils.isEmpty(expiryDate)) {
            etExpiryDate.setError("Please enter expiry date");
            etExpiryDate.requestFocus();
            return;
        }

        dbAdapter.open();

        int result = dbAdapter.updateFoodDonation(
                offerId,
                foodName,
                quantity,
                description,
                expiryDate
        );

        dbAdapter.close();

        if (result > 0) {

            Toast.makeText(
                    this,
                    "Offer updated successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to update offer",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}