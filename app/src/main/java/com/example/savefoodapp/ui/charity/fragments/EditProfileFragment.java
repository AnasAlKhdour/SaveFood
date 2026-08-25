package com.example.savefoodapp.ui.charity.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.OrganizationRepository;
import com.example.savefoodapp.data.repository.UserRepository;

import java.util.Locale;

public class EditProfileFragment
        extends Fragment {

    private AppCompatEditText etOrganizationName;
    private AppCompatEditText etOrganizationPhone;
    private AppCompatEditText etEmail;

    private com.google.android.material.button.MaterialButton
            btnSaveProfile;

    private com.google.android.material.button.MaterialButton
            btnCancelEditProfile;

    private SessionManager sessionManager;

    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    private User currentUser;

    private String currentOrganizationName = "";
    private String currentOrganizationPhone = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_edit_profile,
                container,
                false
        );
    }

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

        etOrganizationName =
                view.findViewById(
                        R.id.etEditOrganizationName
                );

        etOrganizationPhone =
                view.findViewById(
                        R.id.etEditOrganizationPhone
                );

        etEmail =
                view.findViewById(
                        R.id.etEditProfileEmail
                );

        btnSaveProfile =
                view.findViewById(
                        R.id.btnSaveProfile
                );

        btnCancelEditProfile =
                view.findViewById(
                        R.id.btnCancelEditProfile
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
        // Load Data
        // ------------------------------------------------

        loadProfileData();

        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        btnSaveProfile.setOnClickListener(
                v -> saveChanges()
        );

        btnCancelEditProfile.setOnClickListener(
                v -> requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    // ====================================================
    // LOAD PROFILE DATA
    // ====================================================

    private void loadProfileData() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

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

        if (!"Charity Organization".equals(
                currentUser.getRole()
        )) {

            showError(
                    "Invalid account type"
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
                    "Unable to load organization"
            );

            return;
        }

        currentOrganizationName =
                organization[0];

        currentOrganizationPhone =
                organization[1];

        // ------------------------------------------------
        // Display
        // ------------------------------------------------

        etOrganizationName.setText(
                currentOrganizationName
        );

        etOrganizationPhone.setText(
                currentOrganizationPhone
        );

        etEmail.setText(
                currentUser.getEmail()
        );

        etEmail.setEnabled(
                false
        );
    }

    // ====================================================
    // SAVE CHANGES
    // ====================================================

    private void saveChanges() {

        if (currentUser == null) {

            return;
        }

        String newName =
                etOrganizationName
                        .getText()
                        .toString()
                        .trim();

        String newPhone =
                etOrganizationPhone
                        .getText()
                        .toString()
                        .trim();

        // ------------------------------------------------
        // Validate Name
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                newName
        )) {

            etOrganizationName.setError(
                    "Organization name is required"
            );

            etOrganizationName.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Validate Phone
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                newPhone
        )) {

            etOrganizationPhone.setError(
                    "Phone number is required"
            );

            etOrganizationPhone.requestFocus();

            return;
        }

        // ------------------------------------------------
        // No Changes
        // ------------------------------------------------

        if (newName.equals(
                currentOrganizationName
        )
                && newPhone.equals(
                currentOrganizationPhone
        )) {

            Toast.makeText(
                    requireContext(),
                    "No changes to save",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Update
        // ------------------------------------------------

        boolean updated =
                organizationRepository
                        .updateCharityProfile(
                                currentUser.getId(),
                                currentUser.getOrganizationId(),
                                newName,
                                newPhone
                        );

        if (!updated) {

            Toast.makeText(
                    requireContext(),
                    "Failed to update profile",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                requireContext(),
                "Profile updated successfully",
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

        if (userRepository != null) {

            userRepository.close();
        }

        if (organizationRepository != null) {

            organizationRepository.close();
        }

        super.onDestroyView();
    }
}