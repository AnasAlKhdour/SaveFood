package com.example.savefoodapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.utils.SessionManager;

import java.util.ArrayList;

public class IncomingRequestsActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private TextView tvEmptyRequests;
    private Button btnBack;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    private ArrayList<String> requestsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_incoming_requests);

        listViewRequests = findViewById(R.id.listViewRequests);
        tvEmptyRequests = findViewById(R.id.tvEmptyRequests);
        btnBack = findViewById(R.id.btnBack);

        sessionManager = new SessionManager(this);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        requestsList = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                requestsList
        );

        listViewRequests.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadIncomingRequests();

        // Open request details
        listViewRequests.setOnItemClickListener((parent, view, position, id) -> {

            Cursor cursor = dbAdapter.getRequestsByInstitution(
                    sessionManager.getUserId()
            );

            if (cursor != null && cursor.moveToPosition(position)) {

                int requestId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("id")
                        );

                int donationId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("donation_id")
                        );

                int quantity =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("quantity_requested")
                        );

                String status =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("status")
                        );

                android.content.Intent intent =
                        new android.content.Intent(
                                IncomingRequestsActivity.this,
                                RequestDetailsActivity.class
                        );

                intent.putExtra("REQUEST_ID", requestId);
                intent.putExtra("DONATION_ID", donationId);
                intent.putExtra("QUANTITY", quantity);
                intent.putExtra("STATUS", status);

                startActivity(intent);

                cursor.close();
            }
        });
    }

    private void loadIncomingRequests() {

        int institutionId = sessionManager.getUserId();

        Cursor cursor =
                dbAdapter.getRequestsByInstitution(institutionId);

        requestsList.clear();

        if (cursor != null && cursor.moveToFirst()) {

            tvEmptyRequests.setVisibility(TextView.GONE);
            listViewRequests.setVisibility(ListView.VISIBLE);

            do {

                int requestId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("id")
                        );

                int donationId =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("donation_id")
                        );

                int quantity =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("quantity_requested")
                        );

                String status =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("status")
                        );

                String request =
                        "Request #" + requestId +
                                "\nDonation ID: " + donationId +
                                "\nRequested Quantity: " + quantity +
                                "\nStatus: " + status;

                requestsList.add(request);

            } while (cursor.moveToNext());

            cursor.close();

        } else {

            tvEmptyRequests.setVisibility(TextView.VISIBLE);
            listViewRequests.setVisibility(ListView.GONE);

            if (cursor != null) {
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();
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