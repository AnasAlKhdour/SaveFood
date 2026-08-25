package com.example.savefoodapp.ui.food.fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.image.ImageUtils;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.OrganizationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.auth.LoginActivity;
import com.example.savefoodapp.ui.food.FoodMainActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileLocation;

    private Button btnEditProfile;
    private Button btnChangePassword;
    private Button btnChangeLocation;
    private Button btnLogout;
    private Button btnDeleteAccount;

    // ====================================================
    // Data
    // ====================================================

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    private User currentUser;

    // ====================================================
    // ADDRESS LOOKUP
    // ====================================================

    private final ExecutorService geocoderExecutor =
            Executors.newSingleThreadExecutor();

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
                R.layout.fragment_food_profile,
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

        initializeViews(view);

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // User Repository
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        userRepository.open();

        // ------------------------------------------------
        // Organization Repository
        // ------------------------------------------------

        organizationRepository =
                new OrganizationRepository(
                        requireContext()
                );

        organizationRepository.open();

        // ------------------------------------------------
        // Load Profile
        // ------------------------------------------------

        loadProfile();

        // ------------------------------------------------
        // Listeners
        // ------------------------------------------------

        setupClickListeners();
    }

    // ====================================================
    // INITIALIZE VIEWS
    // ====================================================

    private void initializeViews(
            View view
    ) {

        tvProfileName =
                view.findViewById(
                        R.id.tvProfileName
                );

        tvProfileRole =
                view.findViewById(
                        R.id.tvProfileRole
                );

        tvProfileEmail =
                view.findViewById(
                        R.id.tvProfileEmail
                );

        tvProfilePhone =
                view.findViewById(
                        R.id.tvProfilePhone
                );

        tvProfileLocation =
                view.findViewById(
                        R.id.tvProfileLocation
                );

        btnEditProfile =
                view.findViewById(
                        R.id.btnEditProfile
                );

        btnChangePassword =
                view.findViewById(
                        R.id.btnChangePassword
                );

        btnChangeLocation =
                view.findViewById(
                        R.id.btnChangeLocation
                );

        btnLogout =
                view.findViewById(
                        R.id.btnProfileLogout
                );

        btnDeleteAccount =
                view.findViewById(
                        R.id.btnDeleteAccount
                );
    }

    // ====================================================
    // LOAD PROFILE
    // ====================================================

    private void loadProfile() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        currentUser =
                userRepository.getUser(
                        email
                );

        if (currentUser == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Food Institution".equals(
                currentUser.getRole()
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
                currentUser.getOrganizationId();

        if (organizationId <= 0) {

            showError(
                    "Food organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Load Organization
        // ------------------------------------------------

        if (organizationRepository == null) {

            showError(
                    "Organization repository is not initialized"
            );

            return;
        }

        String[] organization =
                organizationRepository
                        .getFoodOrganizationDetails(
                                organizationId
                        );

        if (organization == null) {

            showError(
                    "Unable to load organization information"
            );

            return;
        }

        // ------------------------------------------------
        // Organization Name
        // organization[0] = name
        // ------------------------------------------------

        String organizationName =
                organization[0];

        if (TextUtils.isEmpty(
                organizationName
        )) {

            organizationName =
                    "Food Institution";
        }

        tvProfileName.setText(
                organizationName
        );

        // ------------------------------------------------
        // Role
        // ------------------------------------------------

        tvProfileRole.setText(
                currentUser.getRole()
        );

        // ------------------------------------------------
        // Email
        // ------------------------------------------------

        tvProfileEmail.setText(
                currentUser.getEmail()
        );

        // ------------------------------------------------
        // Phone
        // organization[1] = phone
        // ------------------------------------------------

        String phone =
                organization[1];

        if (TextUtils.isEmpty(phone)) {

            phone =
                    "Phone not set";
        }

        tvProfilePhone.setText(
                phone
        );

        // ------------------------------------------------
        // Location
        //
        // organization[3] = latitude
        // organization[4] = longitude
        //
        // Coordinates remain in the database,
        // but the user sees a readable address.
        // ------------------------------------------------

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

                tvProfileLocation.setText(
                        "Location not set"
                );

            } else {

                tvProfileLocation.setText(
                        "Loading location..."
                );

                loadReadableAddress(
                        latitude,
                        longitude
                );
            }

        } catch (Exception e) {

            tvProfileLocation.setText(
                    "Location not available"
            );
        }
    }

    // ====================================================
    // LOAD READABLE ADDRESS
    // ====================================================

    private void loadReadableAddress(
            double latitude,
            double longitude
    ) {

        if (getContext() == null) {
            return;
        }

        final android.content.Context
                applicationContext =
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
                                                || tvProfileLocation
                                                == null) {

                                            return;
                                        }

                                        tvProfileLocation.setText(
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
            android.content.Context context,
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

            String locality =
                    address.getLocality();

            String country =
                    address.getCountryName();

            // ------------------------------------------------
            // Preferred display:
            // City, Country
            // ------------------------------------------------

            if (!TextUtils.isEmpty(locality)
                    && !TextUtils.isEmpty(country)) {

                return locality
                        + ", "
                        + country;
            }

            // ------------------------------------------------
            // If city is unavailable,
            // use sub-admin area
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
            // Fallback to full address
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
    // CLICK LISTENERS
    // ====================================================

    private void setupClickListeners() {

        // ------------------------------------------------
        // Edit Profile
        // ------------------------------------------------

        btnEditProfile.setOnClickListener(
                view -> {

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(
                                    R.id.fragmentContainer,
                                    new EditProfileFragment()
                            )
                            .addToBackStack(
                                    "food_edit_profile"
                            )
                            .commit();

                    updateToolbar(
                            "Edit Profile"
                    );
                }
        );

        // ------------------------------------------------
        // Change Password
        // ------------------------------------------------

        btnChangePassword.setOnClickListener(
                view -> {

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(
                                    R.id.fragmentContainer,
                                    new ChangePasswordFragment()
                            )
                            .addToBackStack(
                                    "food_change_password"
                            )
                            .commit();

                    updateToolbar(
                            "Change Password"
                    );
                }
        );

        // ------------------------------------------------
        // Change Location
        // ------------------------------------------------

        btnChangeLocation.setOnClickListener(
                view -> {

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(
                                    R.id.fragmentContainer,
                                    new ChangeLocationFragment()
                            )
                            .addToBackStack(
                                    "food_change_location"
                            )
                            .commit();

                    updateToolbar(
                            "Change Location"
                    );
                }
        );

        // ------------------------------------------------
        // Logout
        // ------------------------------------------------

        btnLogout.setOnClickListener(
                view -> logout()
        );

        // ------------------------------------------------
        // Delete Account
        // ------------------------------------------------

        btnDeleteAccount.setOnClickListener(
                view ->
                        showDeleteAccountConfirmation()
        );
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
// DELETE ACCOUNT CONFIRMATION
// ====================================================

    private void showDeleteAccountConfirmation() {

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Delete Account?"
                        )
                        .setMessage(
                                "This action is permanent.\n\n"
                                        + "Your account, organization profile, "
                                        + "and your food donation requests "
                                        + "will be deleted."
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Continue",
                                (dialogInterface, which) ->
                                        showFinalDeleteConfirmation()
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    // ------------------------------------------------
                    // Dialog Background
                    // ------------------------------------------------

                    if (dialog.getWindow() != null) {

                        dialog.getWindow().setBackgroundDrawable(
                                new android.graphics.drawable.ColorDrawable(
                                        android.graphics.Color.parseColor(
                                                "#424242"
                                        )
                                )
                        );
                    }

                    // ------------------------------------------------
                    // Title
                    // ------------------------------------------------

                    TextView title =
                            dialog.findViewById(
                                    androidx.appcompat.R.id.alertTitle
                            );

                    if (title != null) {

                        title.setTextColor(
                                android.graphics.Color.WHITE
                        );
                    }

                    // ------------------------------------------------
                    // Message
                    // ------------------------------------------------

                    TextView message =
                            dialog.findViewById(
                                    android.R.id.message
                            );

                    if (message != null) {

                        message.setTextColor(
                                android.graphics.Color.WHITE
                        );
                    }

                    // ------------------------------------------------
                    // Buttons
                    // ------------------------------------------------

                    Button negativeButton =
                            dialog.getButton(
                                    androidx.appcompat.app.AlertDialog
                                            .BUTTON_NEGATIVE
                            );

                    Button positiveButton =
                            dialog.getButton(
                                    androidx.appcompat.app.AlertDialog
                                            .BUTTON_POSITIVE
                            );

                    int green =
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(),
                                    R.color.savefood_green
                            );

                    if (negativeButton != null) {

                        negativeButton.setTextColor(
                                green
                        );
                    }

                    if (positiveButton != null) {

                        positiveButton.setTextColor(
                                green
                        );
                    }
                }
        );

        dialog.show();
    }


// ====================================================
// FINAL DELETE ACCOUNT CONFIRMATION
// ====================================================

    private void showFinalDeleteConfirmation() {

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Delete Account"
                        )
                        .setMessage(
                                "Are you sure you want to delete "
                                        + "your account?\n\n"
                                        + "This will permanently delete "
                                        + "your organization, food offers, "
                                        + "requests, and account data."
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Delete",
                                (dialogInterface, which) ->
                                        deleteAccount()
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    // ------------------------------------------------
                    // Dialog Background
                    // ------------------------------------------------

                    if (dialog.getWindow() != null) {

                        dialog.getWindow().setBackgroundDrawable(
                                new android.graphics.drawable.ColorDrawable(
                                        android.graphics.Color.parseColor(
                                                "#424242"
                                        )
                                )
                        );
                    }

                    // ------------------------------------------------
                    // Title
                    // ------------------------------------------------

                    TextView title =
                            dialog.findViewById(
                                    androidx.appcompat.R.id.alertTitle
                            );

                    if (title != null) {

                        title.setTextColor(
                                android.graphics.Color.WHITE
                        );
                    }

                    // ------------------------------------------------
                    // Message
                    // ------------------------------------------------

                    TextView message =
                            dialog.findViewById(
                                    android.R.id.message
                            );

                    if (message != null) {

                        message.setTextColor(
                                android.graphics.Color.WHITE
                        );
                    }

                    // ------------------------------------------------
                    // Buttons
                    // ------------------------------------------------

                    Button negativeButton =
                            dialog.getButton(
                                    androidx.appcompat.app.AlertDialog
                                            .BUTTON_NEGATIVE
                            );

                    Button positiveButton =
                            dialog.getButton(
                                    androidx.appcompat.app.AlertDialog
                                            .BUTTON_POSITIVE
                            );

                    int green =
                            androidx.core.content.ContextCompat.getColor(
                                    requireContext(),
                                    R.color.savefood_green
                            );

                    if (negativeButton != null) {

                        negativeButton.setTextColor(
                                green
                        );
                    }

                    if (positiveButton != null) {

                        positiveButton.setTextColor(
                                green
                        );
                    }
                }
        );

        dialog.show();
    }

    // ====================================================
    // DELETE ACCOUNT
    // ====================================================

    private void deleteAccount() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

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

        if (!"Food Institution".equals(
                user.getRole()
        )) {

            showError(
                    "Invalid account type"
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
        // Delete Database Data
        // ------------------------------------------------

        java.util.List<String> imagePaths =
                organizationRepository
                        .deleteFoodOrganizationAccount(
                                userId,
                                organizationId
                        );

        if (imagePaths == null) {

            showError(
                    "Failed to delete account"
            );

            return;
        }

        // ------------------------------------------------
        // Delete Images
        // ------------------------------------------------

        for (String imagePath :
                imagePaths) {

            ImageUtils.deleteImage(
                    imagePath
            );
        }

        // ------------------------------------------------
        // Logout
        // ------------------------------------------------

        sessionManager.logout();

        Toast.makeText(
                requireContext(),
                "Account deleted successfully",
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
        // Return To Login
        // ------------------------------------------------

        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // ====================================================
    // LOGOUT
    // ====================================================

    private void logout() {

        sessionManager.logout();

        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }

    // ====================================================
    // REFRESH
    // ====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (sessionManager != null
                && userRepository != null
                && organizationRepository != null) {

            loadProfile();
        }

        updateToolbar(
                "Profile"
        );
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
