package com.example.savefoodapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.utils.SessionManager;

import java.util.ArrayList;

public class MyRequestsActivity extends AppCompatActivity {

    private ListView listRequests;
    private Button btnBack;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    private ArrayList<String> requestsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_requests);

        listRequests = findViewById(R.id.listRequests);
        btnBack = findViewById(R.id.btnBack);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        sessionManager = new SessionManager(this);

        requestsList = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                requestsList
        );

        listRequests.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadMyRequests();
    }

    private void loadMyRequests() {

        int charityId = sessionManager.getUserId();

        Cursor cursor = dbAdapter.getRequestsByCharity(charityId);

        requestsList.clear();

        if (cursor != null && cursor.moveToFirst()) {

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
                                "\nQuantity: " + quantity +
                                "\nStatus: " + status;

                requestsList.add(request);

            } while (cursor.moveToNext());

            cursor.close();

        } else {

            Toast.makeText(
                    this,
                    "No requests found",
                    Toast.LENGTH_SHORT
            ).show();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (dbAdapter != null) {
            dbAdapter.close();
        }
    }
}