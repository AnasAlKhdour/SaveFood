package com.example.savefoodapp.ui.charity.fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.OrganizationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.auth.LoginActivity;
import com.example.savefoodapp.ui.location.LocationPickerActivity;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileLocation;

    private MaterialButton btnEditProfile;
    private MaterialButton btnChangePassword;
    private MaterialButton btnChangeLocation;
    private MaterialButton btnLogout;
    private MaterialButton btnDeleteAccount;

    // ====================================================
    // Core / Data
    // ====================================================

    private SessionManager sessionManager;

    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    private User currentUser;
    private ActivityResultLauncher<Intent>
            locationPickerLauncher;

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
                R.layout.fragment_profile,
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
        // Initialize Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Initialize User Repository
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        userRepository.open();

        // ------------------------------------------------
        // Initialize Organization Repository
        // ------------------------------------------------

        organizationRepository =
                new OrganizationRepository(
                        requireContext()
                );

        organizationRepository.open();

        // ------------------------------------------------
// Location Picker Launcher
// ------------------------------------------------

        locationPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == requireActivity().RESULT_OK
                                    && result.getData() != null) {

                                Intent data =
                                        result.getData();

                                double latitude =
                                        data.getDoubleExtra(
                                                "latitude",
                                                0.0
                                        );

                                double longitude =
                                        data.getDoubleExtra(
                                                "longitude",
                                                0.0
                                        );

                                updateCharityLocation(
                                        latitude,
                                        longitude
                                );
                            }
                        }
                );

        // ------------------------------------------------
        // Load Profile
        // ------------------------------------------------

        loadProfile();

        // ------------------------------------------------
        // Setup Buttons
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
    // SETUP CLICK LISTENERS
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
                                    "edit_profile"
                            )
                            .commit();

                    if (requireActivity()
                            instanceof CharityMainActivity) {

                        ((CharityMainActivity)
                                requireActivity())
                                .setProfileToolbarTitle(
                                        "Edit Profile"
                                );
                    }
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
                                    "change_password"
                            )
                            .commit();

                    if (requireActivity()
                            instanceof CharityMainActivity) {

                        ((CharityMainActivity)
                                requireActivity())
                                .setProfileToolbarTitle(
                                        "Change Password"
                                );
                    }
                }
        );
        // ------------------------------------------------
        // Change Location
        // ------------------------------------------------

        btnChangeLocation.setOnClickListener(
                view -> openChangeLocation()
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
                view -> showDeleteAccountConfirmation()
        );
    }

    // ====================================================
    // LOAD PROFILE
    // ====================================================

    private void loadProfile() {

        // ------------------------------------------------
        // Validate Session
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Load User
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

        if (!"Charity Organization".equals(
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
                    "Charity organization not found"
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
                        .getCharityOrganizationDetails(
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
                    "Charity Organization";
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
                        getReadableLocation(
                                latitude,
                                longitude
                        )
                );
            }

        } catch (Exception e) {

            tvProfileLocation.setText(
                    "Location not available"
            );
        }
    }

    // ====================================================
    // GET READABLE LOCATION
    // ====================================================

    private String getReadableLocation(
            double latitude,
            double longitude
    ) {

        try {

            Geocoder geocoder =
                    new Geocoder(
                            requireContext(),
                            Locale.getDefault()
                    );

            List<Address> addresses =
                    geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1
                    );

            if (addresses != null
                    && !addresses.isEmpty()) {

                Address address =
                        addresses.get(0);

                String locality =
                        address.getLocality();

                String country =
                        address.getCountryName();

                if (locality != null
                        && country != null) {

                    return locality
                            + ", "
                            + country;
                }

                if (locality != null) {

                    return locality;
                }

                if (country != null) {

                    return country;
                }
            }

        } catch (Exception e) {

            // Ignore and use fallback
        }

        return "Location set";
    }

    // ====================================================
    // ON RESUME
    // ====================================================

    @Override
    public void onResume() {

        super.onResume();

        /*
         * Refresh profile when returning from
         * EditProfileFragment.
         */

        if (sessionManager != null
                && userRepository != null
                && organizationRepository != null) {

            loadProfile();
        }

        if (requireActivity()
                instanceof CharityMainActivity) {

            ((CharityMainActivity)
                    requireActivity())
                    .setProfileToolbarTitle(
                            "Profile"
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
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (userRepository != null) {

            userRepository.close();
            userRepository = null;
        }

        if (organizationRepository != null) {

            organizationRepository.close();
            organizationRepository = null;
        }

        super.onDestroyView();
    }

    // ====================================================
// OPEN CHANGE LOCATION
// ====================================================

    private void openChangeLocation() {

        if (currentUser == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        int organizationId =
                currentUser.getOrganizationId();

        if (organizationId <= 0) {

            showError(
                    "Charity organization not found"
            );

            return;
        }

        String[] organization =
                organizationRepository
                        .getCharityOrganizationDetails(
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

            Intent intent =
                    new Intent(
                            requireContext(),
                            LocationPickerActivity.class
                    );

            intent.putExtra(
                    "INITIAL_LATITUDE",
                    latitude
            );

            intent.putExtra(
                    "INITIAL_LONGITUDE",
                    longitude
            );

            intent.putExtra(
                    "HAS_INITIAL_LOCATION",
                    !(latitude == 0.0
                            && longitude == 0.0)
            );

            locationPickerLauncher.launch(
                    intent
            );

        } catch (Exception e) {

            showError(
                    "Unable to read current location"
            );
        }
    }
    // ====================================================
// DELETE ACCOUNT CONFIRMATION
// ====================================================

    private void showDeleteAccountConfirmation() {

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Delete Account?"
                        )
                        .setMessage(
                                "This action is permanent.\n\n"
                                        + "Your account, organization profile, "
                                        + "and your food donation requests will be deleted."
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Continue",
                                (dialogInterface, which) ->
                                        showDeletePasswordDialog()
                        )
                        .create();

        // ------------------------------------------------
        // Show Dialog
        // ------------------------------------------------

        dialog.show();

        // ------------------------------------------------
        // Dialog Background
        // ------------------------------------------------

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            requireContext().getColor(
                                    R.color.savefood_surface
                            )
                    )
            );
        }

        // ------------------------------------------------
        // Dialog Title
        // ------------------------------------------------

        TextView title =
                dialog.findViewById(
                        androidx.appcompat.R.id.alertTitle
                );

        if (title != null) {

            title.setTextColor(
                    requireContext().getColor(
                            R.color.savefood_text_primary
                    )
            );
        }

        // ------------------------------------------------
        // Dialog Message
        // ------------------------------------------------

        TextView message =
                dialog.findViewById(
                        android.R.id.message
                );

        if (message != null) {

            message.setTextColor(
                    requireContext().getColor(
                            R.color.savefood_text_primary
                    )
            );
        }

        // ------------------------------------------------
        // Positive Button
        // ------------------------------------------------

        dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ).setTextColor(
                requireContext().getColor(
                        R.color.savefood_green
                )
        );

        // ------------------------------------------------
        // Negative Button
        // ------------------------------------------------

        dialog.getButton(
                AlertDialog.BUTTON_NEGATIVE
        ).setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_secondary
                )
        );
    }

// ====================================================
// DELETE ACCOUNT - PASSWORD
// ====================================================

    private void showDeletePasswordDialog() {

        TextInputLayout
                passwordLayout =
                new TextInputLayout(
                        requireContext()
                );

        passwordLayout.setHint(
                "Current Password"
        );

        passwordLayout.setBoxBackgroundMode(
                TextInputLayout
                        .BOX_BACKGROUND_OUTLINE
        );

        passwordLayout.setEndIconMode(
                TextInputLayout
                        .END_ICON_PASSWORD_TOGGLE
        );

        int padding =
                (int) (20 * getResources()
                        .getDisplayMetrics()
                        .density);

        passwordLayout.setPadding(
                padding,
                0,
                padding,
                0
        );

        TextInputEditText
                passwordEditText =
                new TextInputEditText(
                        requireContext()
                );

        passwordEditText.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        passwordLayout.addView(
                passwordEditText
        );

        // ------------------------------------------------
        // Create Dialog
        // ------------------------------------------------

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Confirm Account Deletion"
                        )
                        .setMessage(
                                "Enter your current password to permanently delete your account."
                        )
                        .setView(
                                passwordLayout
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Delete Account",
                                null
                        )
                        .create();

        // ------------------------------------------------
        // Show Dialog
        // ------------------------------------------------

        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                            AlertDialog
                                    .BUTTON_POSITIVE
                    ).setOnClickListener(
                            view -> {

                                String password =
                                        passwordEditText
                                                .getText()
                                                .toString();

                                if (password.isEmpty()) {

                                    passwordEditText.setError(
                                            "Enter your current password"
                                    );

                                    return;
                                }

                                deleteAccount(
                                        password,
                                        dialog
                                );
                            }
                    );
                }
        );

        // ------------------------------------------------
        // IMPORTANT: Actually show dialog
        // ------------------------------------------------

        dialog.show();
    }


    // ====================================================
    // DELETE ACCOUNT
    // ====================================================

    private void deleteAccount(
            String currentPassword,
            AlertDialog dialog
    ) {

        if (currentUser == null) {

            showError(
                    "Unable to load user"
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

        int userId =
                currentUser.getId();

        int organizationId =
                currentUser.getOrganizationId();

        if (userId <= 0
                || organizationId <= 0) {

            showError(
                    "Invalid account information"
            );

            return;
        }

        // ------------------------------------------------
        // Delete Everything
        // ------------------------------------------------

        boolean deleted =
                organizationRepository
                        .deleteCharityAccount(
                                userId,
                                organizationId,
                                email,
                                currentPassword
                        );

        if (!deleted) {

            Toast.makeText(
                    requireContext(),
                    "Incorrect password or account deletion failed",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        dialog.dismiss();

        Toast.makeText(
                requireContext(),
                "Account deleted successfully",
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
        // Clear Session
        // ------------------------------------------------

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
    // UPDATE CHARITY LOCATION
    // ====================================================

    private void updateCharityLocation(
            double latitude,
            double longitude
    ) {

        if (currentUser == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        int organizationId =
                currentUser.getOrganizationId();

        boolean updated =
                organizationRepository
                        .updateCharityOrganizationLocation(
                                currentUser.getId(),
                                organizationId,
                                latitude,
                                longitude
                        );

        if (!updated) {

            Toast.makeText(
                    requireContext(),
                    "Failed to update location",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                requireContext(),
                "Location updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        loadProfile();
    }
}
