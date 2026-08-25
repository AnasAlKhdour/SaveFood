package com.example.savefoodapp.ui.food.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.OrganizationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextInputEditText etOrganizationName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;

    private MaterialButton btnSaveChanges;
    private MaterialButton btnCancel;

    // ====================================================
    // Data
    // ====================================================

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;

    // ====================================================
    // VIEW
    // ====================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_edit_profile_food,
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
        // Initialize Views
        // ------------------------------------------------

        etOrganizationName =
                view.findViewById(
                        R.id.etOrganizationName
                );

        etEmail =
                view.findViewById(
                        R.id.etEmail
                );

        etPhone =
                view.findViewById(
                        R.id.etPhone
                );

        btnSaveChanges =
                view.findViewById(
                        R.id.btnSaveChanges
                );

        btnCancel =
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
        // Load Current Data
        // ------------------------------------------------

        loadCurrentData();

        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        btnSaveChanges.setOnClickListener(
                view1 -> saveChanges()
        );

        btnCancel.setOnClickListener(
                view1 ->
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack()
        );

        updateToolbar(
                "Edit Profile"
        );
    }

    // ====================================================
    // LOAD CURRENT DATA
    // ====================================================

    private void loadCurrentData() {

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

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            showError(
                    "Food organization not found"
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

        // organization[0] = name
        etOrganizationName.setText(
                organization[0]
        );

        // Email is read-only
        etEmail.setText(
                user.getEmail()
        );

        // organization[1] = phone
        etPhone.setText(
                organization[1]
        );
    }

    // ====================================================
    // SAVE CHANGES
    // ====================================================

    private void saveChanges() {

        String name =
                etOrganizationName
                        .getText()
                        .toString()
                        .trim();

        String phone =
                etPhone
                        .getText()
                        .toString()
                        .trim();

        // ------------------------------------------------
        // Validate Name
        // ------------------------------------------------

        if (TextUtils.isEmpty(name)) {

            etOrganizationName.setError(
                    "Please enter organization name"
            );

            etOrganizationName.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Validate Phone
        // ------------------------------------------------

        if (TextUtils.isEmpty(phone)) {

            etPhone.setError(
                    "Please enter phone number"
            );

            etPhone.requestFocus();

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
        // Update
        // ------------------------------------------------

        boolean updated =
                organizationRepository
                        .updateFoodProfile(
                                userId,
                                organizationId,
                                name,
                                phone
                        );

        if (!updated) {

            showError(
                    "Failed to save changes"
            );

            return;
        }

        // ------------------------------------------------
        // Update Session Name
        // ------------------------------------------------

        Toast.makeText(
                requireContext(),
                "Profile updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();

        Toast.makeText(
                requireContext(),
                "Profile updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
        // Return
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
}