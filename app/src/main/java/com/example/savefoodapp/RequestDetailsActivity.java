package com.example.savefoodapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView tvRequestId;
    private TextView tvDonationId;
    private TextView tvQuantity;
    private TextView tvStatus;

    private Button btnBack;
    private Button btnAccept;
    private Button btnReject;

    private DBAdapter dbAdapter;

    private int requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_details);

        tvRequestId = findViewById(R.id.tvRequestId);
        tvDonationId = findViewById(R.id.tvDonationId);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack = findViewById(R.id.btnBack);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        requestId = getIntent().getIntExtra("REQUEST_ID", -1);

        int donationId = getIntent().getIntExtra("DONATION_ID", -1);
        int quantity = getIntent().getIntExtra("QUANTITY", 0);
        String status = getIntent().getStringExtra("STATUS");

        tvRequestId.setText("Request ID: " + requestId);
        tvDonationId.setText("Offer ID: " + donationId);
        tvQuantity.setText("Requested Quantity: " + quantity);
        tvStatus.setText("Status: " + status);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        btnBack.setOnClickListener(v -> finish());

        btnAccept.setOnClickListener(v -> updateStatus("ACCEPTED"));

        btnReject.setOnClickListener(v -> updateStatus("REJECTED"));
    }

    private void updateStatus(String newStatus) {

        int result = dbAdapter.updateRequestStatus(
                requestId,
                newStatus
        );

        if (result > 0) {

            tvStatus.setText("Status: " + newStatus);

            Toast.makeText(
                    this,
                    "Request " + newStatus,
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Failed to update request",
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