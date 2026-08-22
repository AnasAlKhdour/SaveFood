package com.example.savefoodapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class OfferDetailsActivity extends AppCompatActivity {

    private TextView tvFoodName;
    private TextView tvQuantity;
    private TextView tvDescription;
    private TextView tvExpiryDate;
    private TextView tvStatus;
    private Button btnRequestFood;

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_offer_details);

        tvFoodName = findViewById(R.id.tvFoodName);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvDescription = findViewById(R.id.tvDescription);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack = findViewById(R.id.btnBack);
        btnRequestFood = findViewById(R.id.btnRequestFood);

        btnRequestFood.setOnClickListener(view -> {

            Intent intent = new Intent(
                    OfferDetailsActivity.this,
                    RequestFoodActivity.class
            );

            intent.putExtra("DONATION_ID",
                    getIntent().getIntExtra("DONATION_ID", 0));

            intent.putExtra("FOOD_NAME",
                    getIntent().getStringExtra("FOOD_NAME"));

            intent.putExtra("QUANTITY",
                    getIntent().getIntExtra("QUANTITY", 0));

            startActivity(intent);
        });

        String foodName = getIntent().getStringExtra("FOOD_NAME");
        int quantity = getIntent().getIntExtra("QUANTITY", 0);
        String description = getIntent().getStringExtra("DESCRIPTION");
        String expiryDate = getIntent().getStringExtra("EXPIRY_DATE");
        String status = getIntent().getStringExtra("STATUS");

        tvFoodName.setText(foodName);
        tvQuantity.setText("Quantity: " + quantity);
        tvDescription.setText("Description: " + description);
        tvExpiryDate.setText("Expiry Date: " + expiryDate);
        tvStatus.setText("Status: " + status);

        btnBack.setOnClickListener(view -> finish());
    }
}
