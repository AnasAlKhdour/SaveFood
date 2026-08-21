package com.example.savefoodapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.utils.LocationHelper;
import com.example.savefoodapp.utils.LocationPermissionHelper;
import com.example.savefoodapp.utils.SessionManager;

public class FoodHomeActivity extends AppCompatActivity {

    private TextView tvRole;
    private TextView tvWelcome;

    private Button btnAddOffer;
    private Button btnMyOffers;
    private Button btnLogout;

    private SessionManager sessionManager;
    private DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);

        btnAddOffer = findViewById(R.id.btnAddOffer);
        btnMyOffers = findViewById(R.id.btnMyOffers);
        btnLogout = findViewById(R.id.btnLogout);

        sessionManager = new SessionManager(this);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Get logged-in user's information
        String userName = sessionManager.getUserName();
        String userRole = sessionManager.getUserRole();

        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome, " + userName);
        }

        if (userRole != null && !userRole.isEmpty()) {
            tvRole.setText(userRole);
        }

        // Get and save Food Organization location
        saveFoodOrganizationLocation();

        // Add Food Offer
        btnAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        FoodHomeActivity.this,
                        AddOfferActivity.class
                );

                startActivity(intent);
            }
        });

        // My Offers
        btnMyOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        FoodHomeActivity.this,
                        MyOffersActivity.class
                );

                startActivity(intent);
            }
        });

        // Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    // Save Food Organization location
    private void saveFoodOrganizationLocation() {

        // Check if location permission is granted
        if (!LocationPermissionHelper.hasLocationPermission(this)) {

            LocationPermissionHelper.requestLocationPermission(this);

            return;
        }

        // Get current location
        LocationHelper.getLocation(
                this,
                new LocationHelper.LocationCallback() {

                    @Override
                    public void onLocationReceived(Location location) {

                        double latitude =
                                LocationHelper.getLatitude(location);

                        double longitude =
                                LocationHelper.getLongitude(location);

                        int userId =
                                sessionManager.getUserId();

                        // Save location in database
                        int rowsUpdated =
                                dbAdapter.updateUserLocation(
                                        userId,
                                        latitude,
                                        longitude
                                );

                        if (rowsUpdated > 0) {

                            Toast.makeText(
                                    FoodHomeActivity.this,
                                    "Location updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    FoodHomeActivity.this,
                                    "Failed to save location",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onLocationFailed() {

                        Toast.makeText(
                                FoodHomeActivity.this,
                                "Unable to get your location",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // Handle Location Permission result
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (LocationPermissionHelper.isLocationPermissionGranted(
                requestCode,
                grantResults
        )) {

            saveFoodOrganizationLocation();

        } else {

            Toast.makeText(
                    this,
                    "Location permission is required",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Logout
    private void logout() {

        sessionManager.logout();

        Intent intent = new Intent(
                FoodHomeActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

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