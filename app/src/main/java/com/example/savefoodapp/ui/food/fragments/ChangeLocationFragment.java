package com.example.savefoodapp.ui.food.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.OrganizationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;
import com.example.savefoodapp.ui.location.LocationPickerActivity;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangeLocationFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextView tvCurrentLocation;
    private TextView tvSelectedLocation;

    private MaterialButton btnChooseLocation;
    private MaterialButton btnSaveLocation;
    private MaterialButton btnCancel;

    // ====================================================
    // Data
    // ====================================================

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    // ====================================================
    // Selected Location
    // ====================================================

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    // ====================================================
    // Address Lookup
    // ====================================================

    private final ExecutorService geocoderExecutor =
            Executors.newSingleThreadExecutor();

    // ====================================================
    // Location Picker Launcher
    // ====================================================

    private final ActivityResultLauncher<Intent>
            locationPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != requireActivity().RESULT_OK) {

                            return;
                        }

                        Intent data =
                                result.getData();

                        if (data == null) {
                            return;
                        }

                        selectedLatitude =
                                data.getDoubleExtra(
                                        "latitude",
                                        0.0
                                );

                        selectedLongitude =
                                data.getDoubleExtra(
                                        "longitude",
                                        0.0
                                );

                        if (selectedLatitude == 0.0
                                && selectedLongitude == 0.0) {

                            Toast.makeText(
                                    requireContext(),
                                    "Invalid location selected",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        // ------------------------------------------------
                        // Show readable address
                        // ------------------------------------------------

                        tvSelectedLocation.setText(
                                "Loading location..."
                        );

                        loadReadableAddress(
                                selectedLatitude,
                                selectedLongitude,
                                tvSelectedLocation
                        );

                        // ------------------------------------------------
                        // Enable Save
                        // ------------------------------------------------

                        btnSaveLocation.setEnabled(
                                true
                        );
                    }
            );

    // ====================================================
    // CREATE VIEW
    // ====================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_change_location,
                container,
                false
        );
    }

    // ====================================================
    // VIEW CREATED
    // ====================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // ------------------------------------------------
        // UI
        // ------------------------------------------------

        tvCurrentLocation =
                view.findViewById(
                        R.id.tvCurrentLocation
                );

        tvSelectedLocation =
                view.findViewById(
                        R.id.tvSelectedLocation
                );

        btnChooseLocation =
                view.findViewById(
                        R.id.btnChooseLocation
                );

        btnSaveLocation =
                view.findViewById(
                        R.id.btnSaveLocation
                );

        btnCancel =
                view.findViewById(
                        R.id.btnCancelChangeLocation
                );

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Repositories
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        organizationRepository =
                new OrganizationRepository(
                        requireContext()
                );

        userRepository.open();
        organizationRepository.open();

        // ------------------------------------------------
        // Load Current Location
        // ------------------------------------------------

        loadCurrentLocation();

        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        btnChooseLocation.setOnClickListener(
                v -> openLocationPicker()
        );

        btnSaveLocation.setOnClickListener(
                v -> saveLocation()
        );

        btnCancel.setOnClickListener(
                v -> requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        btnSaveLocation.setEnabled(
                false
        );

        updateToolbar(
                "Change Location"
        );
    }

    // ====================================================
    // LOAD CURRENT LOCATION
    // ====================================================

    private void loadCurrentLocation() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Food Institution".equals(
                user.getRole()
        )) {

            showError(
                    "Invalid account type"
            );

            return;
        }

        // ------------------------------------------------
        // Organization ID
        // ------------------------------------------------

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            showError(
                    "Food organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Load Organization
        // ------------------------------------------------

        String[] organization =
                organizationRepository
                        .getFoodOrganizationDetails(
                                organizationId
                        );

        if (organization == null) {

            showError(
                    "Unable to load current location"
            );

            return;
        }

        try {

            double latitude =
                    Double.parseDouble(
                            organization[3]
                    );

            double longitude =
                    Double.parseDouble(
                            organization[4]
                    );

            if (latitude == 0.0
                    && longitude == 0.0) {

                tvCurrentLocation.setText(
                        "Location not set"
                );

            } else {

                tvCurrentLocation.setText(
                        "Loading location..."
                );

                loadReadableAddress(
                        latitude,
                        longitude,
                        tvCurrentLocation
                );
            }

        } catch (Exception e) {

            tvCurrentLocation.setText(
                    "Location not available"
            );
        }
    }

    // ====================================================
    // LOAD READABLE ADDRESS
    // ====================================================

    private void loadReadableAddress(
            double latitude,
            double longitude,
            TextView targetView
    ) {

        if (targetView == null) {
            return;
        }

        if (getContext() == null) {
            return;
        }

        Context applicationContext =
                requireContext()
                        .getApplicationContext();

        geocoderExecutor.execute(
                () -> {

                    String addressText =
                            getReadableAddress(
                                    applicationContext,
                                    latitude,
                                    longitude
                            );

                    if (!isAdded()) {
                        return;
                    }

                    requireActivity()
                            .runOnUiThread(
                                    () -> {

                                        if (!isAdded()
                                                || targetView == null) {

                                            return;
                                        }

                                        targetView.setText(
                                                addressText
                                        );
                                    }
                            );
                }
        );
    }

    // ====================================================
    // GET READABLE ADDRESS
    // ====================================================

    private String getReadableAddress(
            Context context,
            double latitude,
            double longitude
    ) {

        Geocoder geocoder =
                new Geocoder(
                        context,
                        Locale.getDefault()
                );

        try {

            List<Address> addresses =
                    geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1
                    );

            if (addresses == null
                    || addresses.isEmpty()) {

                return "Address unavailable";
            }

            Address address =
                    addresses.get(0);

            // ------------------------------------------------
            // Preferred: City, Country
            // ------------------------------------------------

            String locality =
                    address.getLocality();

            String country =
                    address.getCountryName();

            if (!TextUtils.isEmpty(locality)
                    && !TextUtils.isEmpty(country)) {

                return locality
                        + ", "
                        + country;
            }

            // ------------------------------------------------
            // Fallback: Administrative Area, Country
            // ------------------------------------------------

            String subAdminArea =
                    address.getSubAdminArea();

            if (!TextUtils.isEmpty(
                    subAdminArea
            )
                    && !TextUtils.isEmpty(
                    country
            )) {

                return subAdminArea
                        + ", "
                        + country;
            }

            // ------------------------------------------------
            // Fallback: Full Address
            // ------------------------------------------------

            String addressLine =
                    address.getAddressLine(0);

            if (!TextUtils.isEmpty(
                    addressLine
            )) {

                return addressLine;
            }

            return "Address unavailable";

        } catch (IOException e) {

            return "Address unavailable";
        }
    }

    // ====================================================
    // OPEN LOCATION PICKER
    // ====================================================

    private void openLocationPicker() {

        Intent intent =
                new Intent(
                        requireContext(),
                        LocationPickerActivity.class
                );

        locationPickerLauncher.launch(
                intent
        );
    }

    // ====================================================
    // SAVE LOCATION
    // ====================================================

    private void saveLocation() {

        if (selectedLatitude == 0.0
                && selectedLongitude == 0.0) {

            showError(
                    "Please select a location first"
            );

            return;
        }

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        int userId =
                user.getId();

        int organizationId =
                user.getOrganizationId();

        if (userId <= 0
                || organizationId <= 0) {

            showError(
                    "Invalid account information"
            );

            return;
        }

        // ------------------------------------------------
        // Update Location
        // ------------------------------------------------

        boolean updated =
                organizationRepository
                        .updateFoodOrganizationLocation(
                                userId,
                                organizationId,
                                selectedLatitude,
                                selectedLongitude
                        );

        if (!updated) {

            showError(
                    "Failed to update location"
            );

            return;
        }

        Toast.makeText(
                requireContext(),
                "Location updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
        // Return to Profile
        // ------------------------------------------------

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    // ====================================================
    // TOOLBAR
    // ====================================================

    private void updateToolbar(
            String title
    ) {

        if (requireActivity()
                instanceof FoodMainActivity) {

            ((FoodMainActivity)
                    requireActivity())
                    .updateToolbarTitle(
                            title
                    );
        }
    }

    // ====================================================
    // ERROR
    // ====================================================

    private void showError(
            String message
    ) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // ====================================================
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (organizationRepository != null) {

            organizationRepository.close();
            organizationRepository = null;
        }

        if (userRepository != null) {

            userRepository.close();
            userRepository = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        geocoderExecutor.shutdownNow();

        super.onDestroy();
    }
}