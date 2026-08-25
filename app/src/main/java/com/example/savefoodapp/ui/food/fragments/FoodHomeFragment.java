package com.example.savefoodapp.ui.food.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.location.LocationHelper;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.repository.UserRepository;

public class FoodHomeFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextView tvWelcome;
    private TextView tvRole;

    // ====================================================
    // Core / Data
    // ====================================================

    private SessionManager sessionManager;
    private UserRepository userRepository;

    // ====================================================
    // Location Permission
    // ====================================================

    private ActivityResultLauncher<String>
            locationPermissionLauncher;

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
                R.layout.fragment_food_home,
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
        // Initialize UI
        // ------------------------------------------------

        tvWelcome =
                view.findViewById(
                        R.id.tvWelcome
                );

        tvRole =
                view.findViewById(
                        R.id.tvRole
                );

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Repository
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        userRepository.open();

        // ------------------------------------------------
        // Permission Launcher
        // ------------------------------------------------

        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .RequestPermission(),
                        granted -> {

                            if (granted) {

                                saveFoodOrganizationLocation();

                            } else {

                                Toast.makeText(
                                        requireContext(),
                                        "Location permission is required",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );

        // ------------------------------------------------
        // Display User Information
        // ------------------------------------------------

        displayUserInformation();

        // ------------------------------------------------
        // Save Food Organization Location
        // ------------------------------------------------

        saveFoodOrganizationLocation();
    }

    // ====================================================
    // DISPLAY USER INFORMATION
    // ====================================================

    private void displayUserInformation() {

        String userName =
                sessionManager.getUserName();

        String userRole =
                sessionManager.getUserRole();

        if (userName != null
                && !userName.isEmpty()) {

            tvWelcome.setText(
                    "Welcome, " + userName
            );
        }

        if (userRole != null
                && !userRole.isEmpty()) {

            tvRole.setText(
                    userRole
            );
        }
    }

    // ====================================================
    // SAVE FOOD ORGANIZATION LOCATION
    // ====================================================

    private void saveFoodOrganizationLocation() {

        // ------------------------------------------------
        // Check Permission
        // ------------------------------------------------

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
            );

            return;
        }

        // ------------------------------------------------
        // Get Current Location
        // ------------------------------------------------

        LocationHelper.getLocation(
                requireActivity(),
                new LocationHelper.LocationCallback() {

                    @Override
                    public void onLocationReceived(
                            Location location
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        double latitude =
                                LocationHelper
                                        .getLatitude(
                                                location
                                        );

                        double longitude =
                                LocationHelper
                                        .getLongitude(
                                                location
                                        );

                        int userId =
                                sessionManager
                                        .getUserId();

                        // ------------------------------------------------
                        // Save Location
                        // ------------------------------------------------

                        int rowsUpdated =
                                userRepository
                                        .updateUserLocation(
                                                userId,
                                                latitude,
                                                longitude
                                        );

                        if (rowsUpdated <= 0) {

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to save location",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onLocationFailed() {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Unable to get your location",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // ====================================================
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (userRepository != null) {

            userRepository.close();

            userRepository = null;
        }

        super.onDestroyView();
    }
}