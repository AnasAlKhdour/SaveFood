package com.example.savefoodapp.ui.food.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnChangePassword;
    private MaterialButton btnCancel;

    // ====================================================
    // Data
    // ====================================================

    private SessionManager sessionManager;
    private UserRepository userRepository;

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
                R.layout.fragment_food_change_password,
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

        etCurrentPassword =
                view.findViewById(
                        R.id.etCurrentPassword
                );

        etNewPassword =
                view.findViewById(
                        R.id.etNewPassword
                );

        etConfirmPassword =
                view.findViewById(
                        R.id.etConfirmPassword
                );

        btnChangePassword =
                view.findViewById(
                        R.id.btnChangePassword
                );

        btnCancel =
                view.findViewById(
                        R.id.btnCancelChangePassword
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
        // Buttons
        // ------------------------------------------------

        btnChangePassword.setOnClickListener(
                v -> changePassword()
        );

        btnCancel.setOnClickListener(
                v -> requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        updateToolbar(
                "Change Password"
        );
    }

    // ====================================================
    // CHANGE PASSWORD
    // ====================================================

    private void changePassword() {

        String currentPassword =
                etCurrentPassword
                        .getText()
                        .toString();

        String newPassword =
                etNewPassword
                        .getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword
                        .getText()
                        .toString();

        // ------------------------------------------------
        // Current Password
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                currentPassword
        )) {

            etCurrentPassword.setError(
                    "Please enter your current password"
            );

            etCurrentPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // New Password
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                newPassword
        )) {

            etNewPassword.setError(
                    "Please enter a new password"
            );

            etNewPassword.requestFocus();

            return;
        }

        if (newPassword.length() < 6) {

            etNewPassword.setError(
                    "Password must be at least 6 characters"
            );

            etNewPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Confirm Password
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                confirmPassword
        )) {

            etConfirmPassword.setError(
                    "Please confirm your new password"
            );

            etConfirmPassword.requestFocus();

            return;
        }

        if (!newPassword.equals(
                confirmPassword
        )) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Get Email From Session
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
        // Update Password
        // ------------------------------------------------

        boolean success =
                userRepository.changeUserPassword(
                        email,
                        currentPassword,
                        newPassword
                );

        if (!success) {

            etCurrentPassword.setError(
                    "Current password is incorrect"
            );

            etCurrentPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Success
        // ------------------------------------------------

        Toast.makeText(
                requireContext(),
                "Password changed successfully",
                Toast.LENGTH_SHORT
        ).show();

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

        if (userRepository != null) {

            userRepository.close();
            userRepository = null;
        }

        super.onDestroyView();
    }
}