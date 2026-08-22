package com.example.savefoodapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DatabaseHelper;

public class RequestFoodActivity extends AppCompatActivity {

    private TextView tvFoodName;
    private EditText etRequestedQuantity;
    private Button btnSubmitRequest;

    private DatabaseHelper databaseHelper;

    private int donationId;
    private int availableQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_food);

        tvFoodName = findViewById(R.id.tvFoodName);
        etRequestedQuantity = findViewById(R.id.etRequestedQuantity);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        databaseHelper = new DatabaseHelper(this);

        donationId = getIntent().getIntExtra("DONATION_ID", 0);

        String foodName = getIntent().getStringExtra("FOOD_NAME");

        availableQuantity =
                getIntent().getIntExtra("QUANTITY", 0);

        tvFoodName.setText(foodName);

        btnSubmitRequest.setOnClickListener(v -> {

            String quantityText =
                    etRequestedQuantity.getText().toString().trim();

            // Validate empty quantity
            if (quantityText.isEmpty()) {
                etRequestedQuantity.setError(
                        "Please enter requested quantity"
                );
                return;
            }

            int requestedQuantity =
                    Integer.parseInt(quantityText);

            // Validate quantity
            if (requestedQuantity <= 0) {
                etRequestedQuantity.setError(
                        "Quantity must be greater than 0"
                );
                return;
            }

            // Validate against available quantity
            if (requestedQuantity > availableQuantity) {
                etRequestedQuantity.setError(
                        "Requested quantity is greater than available quantity"
                );
                return;
            }

            // Temporary charity ID
            int charityOrganizationId =
                    getIntent().getIntExtra("CHARITY_ID", 1);

            long result = databaseHelper.insertRequest(
                    donationId,
                    charityOrganizationId,
                    requestedQuantity,
                    "PENDING"
            );

            if (result != -1) {

                Toast.makeText(
                        this,
                        "Request submitted successfully",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to submit request",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}