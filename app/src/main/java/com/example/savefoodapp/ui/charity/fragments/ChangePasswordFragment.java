package com.example.savefoodapp.ui.charity.fragments;

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
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment
        extends Fragment {

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
                R.layout.fragment_change_password,
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

        // ------------------------------------------------
        // Toolbar
        // ------------------------------------------------

        if (requireActivity()
                instanceof CharityMainActivity) {

            ((CharityMainActivity)
                    requireActivity())
                    .setProfileToolbarTitle(
                            "Change Password"
                    );
        }
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
                    "Enter your current password"
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
                    "Enter a new password"
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
                    "Confirm your new password"
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
        // Prevent Same Password
        // ------------------------------------------------

        if (currentPassword.equals(
                newPassword
        )) {

            etNewPassword.setError(
                    "New password must be different"
            );

            etNewPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            Toast.makeText(
                    requireContext(),
                    "User session not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Update Database
        // ------------------------------------------------

        boolean updated =
                userRepository.changeUserPassword(
                        email,
                        currentPassword,
                        newPassword
                );

        if (!updated) {

            Toast.makeText(
                    requireContext(),
                    "Current password is incorrect",
                    Toast.LENGTH_LONG
            ).show();

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