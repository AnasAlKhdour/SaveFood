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

public class CharityHomeActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnAvailableOffers;

    private TextView tvWelcome;

    private SessionManager sessionManager;
    private DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charity_home);

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        btnAvailableOffers = findViewById(R.id.btnAvailableOffers);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize database
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Get charity name from session
        String userName = sessionManager.getUserName();

        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome, " + userName);
        } else {
            tvWelcome.setText("Welcome, Charity");
        }

        // Save Charity location
        saveCharityLocation();

        // Available Offers
        btnAvailableOffers.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(
                                CharityHomeActivity.this,
                                AvailableOffersActivity.class
                        );

                        startActivity(intent);
                    }
                }
        );

        // Logout
        btnLogout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logout();
                    }
                }
        );
    }

    /**
     * Get Charity's current location
     * and save it in the users table.
     */
    private void saveCharityLocation() {

        // Check location permission
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

                        // Get logged-in Charity user ID
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
                                    CharityHomeActivity.this,
                                    "Charity location updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    CharityHomeActivity.this,
                                    "Failed to save charity location",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onLocationFailed() {

                        Toast.makeText(
                                CharityHomeActivity.this,
                                "Unable to get charity location",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    /**
     * Handle location permission result.
     */
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

            // Permission granted -> try again
            saveCharityLocation();

        } else {

            Toast.makeText(
                    this,
                    "Location permission is required",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Logout
     */
    private void logout() {

        sessionManager.logout();

        Intent intent = new Intent(
                CharityHomeActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    /**
     * Close database connection.
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (dbAdapter != null) {
            dbAdapter.close();
        }
    }
}