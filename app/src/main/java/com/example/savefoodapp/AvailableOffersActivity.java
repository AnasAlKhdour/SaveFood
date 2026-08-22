package com.example.savefoodapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AvailableOffersActivity extends AppCompatActivity {

    private ListView listViewOffers;
    private TextView tvEmptyOffers;
    private SearchView searchOffers;
    private Spinner spinnerFilter;
    private Button btnBack;

    private DBAdapter dbAdapter;

    private List<FoodDonation> availableOffers;
    private List<FoodDonation> filteredOffers;

    private double charityLatitude;
    private double charityLongitude;

    private String currentSearch = "";
    private int currentFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_available_offers);

        // Connect UI elements
        listViewOffers = findViewById(R.id.listViewOffers);
        tvEmptyOffers = findViewById(R.id.tvEmptyOffers);
        searchOffers = findViewById(R.id.searchOffers);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnBack = findViewById(R.id.btnBack);

        // Open database
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        // Load Charity location
        loadCharityLocation();

        // Setup filter
        setupFilter();

        // Load available offers
        loadAvailableOffers();

        // Search
        searchOffers.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        currentSearch = query;
                        applySearchAndFilter();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        currentSearch = newText;
                        applySearchAndFilter();

                        return true;
                    }
                }
        );

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Open Offer Details
        listViewOffers.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        FoodDonation selectedOffer =
                                filteredOffers.get(position);

                        Intent intent = new Intent(
                                AvailableOffersActivity.this,
                                OfferDetailsActivity.class
                        );

                        intent.putExtra(
                                "OFFER_ID",
                                selectedOffer.getId()
                        );

                        intent.putExtra(
                                "FOOD_NAME",
                                selectedOffer.getFoodName()
                        );

                        intent.putExtra(
                                "QUANTITY",
                                selectedOffer.getQuantity()
                        );

                        intent.putExtra(
                                "DESCRIPTION",
                                selectedOffer.getDescription()
                        );

                        intent.putExtra(
                                "EXPIRY_DATE",
                                selectedOffer.getExpiryDate()
                        );

                        intent.putExtra(
                                "STATUS",
                                selectedOffer.getStatus()
                        );

                        startActivity(intent);
                    }
                }
        );
    }

    // Get Charity latitude and longitude
    private void loadCharityLocation() {

        SessionManager sessionManager =
                new SessionManager(this);

        String email =
                sessionManager.getUserEmail();

        User charityUser =
                dbAdapter.getUser(email);

        if (charityUser != null) {

            charityLatitude =
                    charityUser.getLatitude();

            charityLongitude =
                    charityUser.getLongitude();

        } else {

            charityLatitude = 0.0;
            charityLongitude = 0.0;
        }
    }

    // T5.2 - Get Available Offers
    private void loadAvailableOffers() {

        availableOffers =
                dbAdapter.getAvailableOffers();

        applySearchAndFilter();
    }

    // T5.5 + T5.6
    // Apply Search and Filter
    private void applySearchAndFilter() {

        if (availableOffers == null) {
            return;
        }

        filteredOffers =
                new ArrayList<>();

        String searchText =
                currentSearch.toLowerCase().trim();

        for (FoodDonation offer : availableOffers) {

            // Search
            boolean matchesSearch =
                    offer.getFoodName()
                            .toLowerCase()
                            .contains(searchText);

            // Filter
            boolean matchesFilter;

            if (currentFilter == 0) {

                // All Offers
                matchesFilter = true;

            } else if (currentFilter == 1) {

                // Quantity <= 5
                matchesFilter =
                        offer.getQuantity() <= 5;

            } else {

                // Quantity > 5
                matchesFilter =
                        offer.getQuantity() > 5;
            }

            if (matchesSearch && matchesFilter) {

                filteredOffers.add(offer);
            }
        }

        displayOffers();
    }

    // T5.6 - Setup Filter
    private void setupFilter() {

        String[] filterOptions = {
                "All Offers",
                "Quantity <= 5",
                "Quantity > 5"
        };

        ArrayAdapter<String> filterAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        filterOptions
                );

        filterAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        currentFilter = position;

                        applySearchAndFilter();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    // T5.3 + T5.8
    // Display Offers and Distance
    private void displayOffers() {

        ArrayList<String> displayList =
                new ArrayList<>();

        for (FoodDonation offer : filteredOffers) {

            // T5.7 - Calculate distance
            double distance =
                    calculateDistanceForOffer(offer);

            String distanceText;

            if (distance >= 0) {

                distanceText =
                        String.format(
                                "Distance: %.2f km",
                                distance
                        );

            } else {

                distanceText =
                        "Distance: Not available";
            }

            String offerText =
                    offer.getFoodName()
                            + "\nQuantity: "
                            + offer.getQuantity()
                            + "\nExpiry Date: "
                            + offer.getExpiryDate()
                            + "\n"
                            + distanceText;

            displayList.add(offerText);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        displayList
                );

        listViewOffers.setAdapter(adapter);

        if (filteredOffers.isEmpty()) {

            listViewOffers.setVisibility(View.GONE);
            tvEmptyOffers.setVisibility(View.VISIBLE);

            if (currentSearch.isEmpty()
                    && currentFilter == 0) {

                tvEmptyOffers.setText(
                        "No available offers"
                );

            } else {

                tvEmptyOffers.setText(
                        "No offers match your search or filter"
                );
            }

        } else {

            listViewOffers.setVisibility(View.VISIBLE);
            tvEmptyOffers.setVisibility(View.GONE);
        }
    }

    // T5.7 - Calculate distance between
// Charity and Food Organization
    private double calculateDistanceForOffer(FoodDonation offer) {

        double[] organizationLocation =
                dbAdapter.getOrganizationLocation(
                        offer.getFoodOrganizationId()
                );

        // Organization location not found
        if (organizationLocation == null) {

            android.util.Log.d(
                    "DISTANCE_TEST",
                    "Organization location is NULL"
            );

            return -1;
        }

        double organizationLatitude =
                organizationLocation[0];

        double organizationLongitude =
                organizationLocation[1];

        // Print all coordinates
        android.util.Log.d(
                "DISTANCE_TEST",
                "================================"
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Charity Latitude = "
                        + charityLatitude
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Charity Longitude = "
                        + charityLongitude
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Organization ID = "
                        + offer.getFoodOrganizationId()
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Organization Latitude = "
                        + organizationLatitude
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Organization Longitude = "
                        + organizationLongitude
        );

        // Calculate distance
        float[] results = new float[1];

        Location.distanceBetween(
                charityLatitude,
                charityLongitude,
                organizationLatitude,
                organizationLongitude,
                results
        );

        double distanceInMeters = results[0];

        double distanceInKm =
                distanceInMeters / 1000.0;

        android.util.Log.d(
                "DISTANCE_TEST",
                "Distance meters = "
                        + distanceInMeters
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "Distance km = "
                        + distanceInKm
        );

        android.util.Log.d(
                "DISTANCE_TEST",
                "================================"
        );

        return distanceInKm;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (dbAdapter != null) {
            dbAdapter.close();
        }
    }
}