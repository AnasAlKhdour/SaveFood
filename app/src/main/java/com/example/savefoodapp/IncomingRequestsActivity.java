package com.example.savefoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.DonationRequest;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class IncomingRequestsActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private TextView tvEmptyRequests;
    private Button btnBack;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    private List<DonationRequest> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_incoming_requests);

        // Initialize UI
        listViewRequests = findViewById(R.id.listViewRequests);
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests);
        btnBack = findViewById(R.id.btnBack);

        // Initialize session
        sessionManager = new SessionManager(this);

        // Initialize database
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Back button
        btnBack.setOnClickListener(view -> finish());

        // Open request details
        listViewRequests.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        DonationRequest selectedRequest =
                                requests.get(position);

                        Intent intent = new Intent(
                                IncomingRequestsActivity.this,
                                RequestDetailsActivity.class
                        );

                        intent.putExtra(
                                "REQUEST_ID",
                                selectedRequest.getId()
                        );

                        startActivity(intent);
                    }
                }
        );

        // Load incoming requests
        loadIncomingRequests();
    }

    private void loadIncomingRequests() {

        String email =
                sessionManager.getUserEmail();

        if (email == null || email.isEmpty()) {

            Toast.makeText(
                    this,
                    "User session not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Get logged-in food organization user
        User user =
                dbAdapter.getUser(email);

        if (user == null) {

            Toast.makeText(
                    this,
                    "Unable to load user",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int foodOrganizationId =
                user.getOrganizationId();

        if (foodOrganizationId <= 0) {

            Toast.makeText(
                    this,
                    "Food organization not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Get requests for this food organization
        requests =
                dbAdapter.getRequestsByInstitution(
                        foodOrganizationId
                );

        displayRequests();
    }

    private void displayRequests() {

        ArrayList<String> displayList =
                new ArrayList<>();

        for (DonationRequest request : requests) {

            String requestText =
                    "Request ID: "
                            + request.getId()
                            + "\nDonation ID: "
                            + request.getDonationId()
                            + "\nRequested Quantity: "
                            + request.getQuantityRequested()
                            + "\nStatus: "
                            + request.getStatus();

            displayList.add(requestText);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        displayList
                );

        listViewRequests.setAdapter(adapter);

        if (requests.isEmpty()) {

            listViewRequests.setVisibility(View.GONE);
            tvEmptyRequests.setVisibility(View.VISIBLE);

        } else {

            listViewRequests.setVisibility(View.VISIBLE);
            tvEmptyRequests.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (dbAdapter != null) {
            loadIncomingRequests();
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