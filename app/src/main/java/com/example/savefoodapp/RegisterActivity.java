package com.example.savefoodapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private RadioGroup rgRole;
    private RadioButton rbFoodInstitution;
    private RadioButton rbCharity;

    private Button btnRegister;
    private TextView tvLogin;

    private DBAdapter dbAdapter;

    // Temporary registration data
    private String pendingName;
    private String pendingEmail;
    private String pendingPhone;
    private String pendingPassword;
    private String pendingRole;

    // Selected location
    private double selectedLatitude;
    private double selectedLongitude;

    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        // ------------------------------------------------
        // Connect UI
        // ------------------------------------------------

        etName =
                findViewById(R.id.etName);

        etEmail =
                findViewById(R.id.etEmail);

        etPhone =
                findViewById(R.id.etPhone);

        etPassword =
                findViewById(R.id.etPassword);

        etConfirmPassword =
                findViewById(R.id.etConfirmPassword);

        rgRole =
                findViewById(R.id.rgRole);

        rbFoodInstitution =
                findViewById(R.id.rbFoodInstitution);

        rbCharity =
                findViewById(R.id.rbCharity);

        btnRegister =
                findViewById(R.id.btnRegister);

        tvLogin =
                findViewById(R.id.tvLogin);

        dbAdapter =
                new DBAdapter(this);

        // ------------------------------------------------
        // Login Link
        // ------------------------------------------------

        tvLogin.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    RegisterActivity.this,
                                    LoginActivity.class
                            );

                    startActivity(intent);

                    finish();
                }
        );

        // ------------------------------------------------
        // Location Permission Result
        // ------------------------------------------------

        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        granted -> {

                            if (granted) {

                                openLocationPicker();

                            } else {

                                Toast.makeText(
                                        this,
                                        R.string.location_permission_required,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );

        // ------------------------------------------------
        // Location Picker Result
        // ------------------------------------------------

        locationPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK
                                    && result.getData() != null) {

                                Intent data =
                                        result.getData();

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

                                completeRegistration();
                            }
                        }
                );

        // ------------------------------------------------
        // Register Button
        // ------------------------------------------------

        btnRegister.setOnClickListener(
                view -> registerUser()
        );
    }

    // ====================================================
    // STEP 1 - Validate Registration Form
    // ====================================================

    private void registerUser() {

        String name =
                etName.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();

        // ------------------------------------------------
        // Name
        // ------------------------------------------------

        if (TextUtils.isEmpty(name)) {

            etName.setError(
                    getString(R.string.enter_name)
            );

            etName.requestFocus();
            return;
        }

        // ------------------------------------------------
        // Email
        // ------------------------------------------------

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    getString(R.string.enter_email)
            );

            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    getString(R.string.valid_email)
            );

            etEmail.requestFocus();
            return;
        }

        // ------------------------------------------------
        // Phone
        // ------------------------------------------------

        if (TextUtils.isEmpty(phone)) {

            etPhone.setError(
                    getString(R.string.enter_phone)
            );

            etPhone.requestFocus();
            return;
        }

        /*
         * Accept normal Jordanian mobile numbers:
         *
         * 079XXXXXXX
         * 078XXXXXXX
         * 077XXXXXXX
         *
         * Exactly 10 digits.
         */

        String normalizedPhone =
                phone.replace(" ", "")
                        .replace("-", "");

        if (!normalizedPhone.matches(
                "07[789][0-9]{7}"
        )) {

            etPhone.setError(
                    getString(R.string.valid_phone)
            );

            etPhone.requestFocus();
            return;
        }

        // ------------------------------------------------
        // Password
        // ------------------------------------------------

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    getString(R.string.enter_password)
            );

            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            etPassword.setError(
                    getString(R.string.password_minimum)
            );

            etPassword.requestFocus();
            return;
        }

        // ------------------------------------------------
        // Confirm Password
        // ------------------------------------------------

        if (TextUtils.isEmpty(confirmPassword)) {

            etConfirmPassword.setError(
                    getString(R.string.confirm_password_required)
            );

            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    getString(R.string.passwords_not_match)
            );

            etConfirmPassword.requestFocus();
            return;
        }

        // ------------------------------------------------
        // Role
        // ------------------------------------------------

        if (rgRole.getCheckedRadioButtonId() == -1) {

            Toast.makeText(
                    this,
                    R.string.select_role,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (rbFoodInstitution.isChecked()) {

            pendingRole =
                    "Food Institution";

        } else {

            pendingRole =
                    "Charity Organization";
        }

        // ------------------------------------------------
        // Save Registration Data Temporarily
        // ------------------------------------------------

        pendingName =
                name;

        pendingEmail =
                email;

        pendingPhone =
                normalizedPhone;

        pendingPassword =
                password;

        // ------------------------------------------------
        // STEP 2 - Show Registration Rules
        // ------------------------------------------------

        showRegistrationRulesDialog();
    }

    // ====================================================
    // STEP 2 - Registration Rules Dialog
    // ====================================================

    private void showRegistrationRulesDialog() {

        Dialog dialog =
                new Dialog(this);

        dialog.setContentView(
                R.layout.dialog_registration_rules
        );

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );

            dialog.getWindow()
                    .setLayout(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    );
        }

        CheckBox cbAgreeRules =
                dialog.findViewById(
                        R.id.cbAgreeRules
                );

        Button btnCancelRules =
                dialog.findViewById(
                        R.id.btnCancelRules
                );

        Button btnContinueRules =
                dialog.findViewById(
                        R.id.btnContinueRules
                );

        // Cancel
        btnCancelRules.setOnClickListener(
                v -> dialog.dismiss()
        );

        // Continue
        btnContinueRules.setOnClickListener(
                v -> {

                    if (!cbAgreeRules.isChecked()) {

                        Toast.makeText(
                                this,
                                R.string.agree_rules_required,
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    dialog.dismiss();

                    // Continue to location permission
                    checkLocationPermission();
                }
        );

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        // Set dialog width after showing
        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setLayout(
                            (int) (
                                    getResources()
                                            .getDisplayMetrics()
                                            .widthPixels * 0.92
                            ),
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    );
        }
    }

    // ====================================================
    // STEP 3 - Location Permission
    // ====================================================

    private void checkLocationPermission() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {

            openLocationPicker();

        } else {

            locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        }
    }

    // ====================================================
    // STEP 4 - Open Location Picker
    // ====================================================

    private void openLocationPicker() {

        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        LocationPickerActivity.class
                );

        locationPickerLauncher.launch(intent);
    }

    // ====================================================
    // STEP 5 - Create Organization + User
    // ====================================================

    private void completeRegistration() {

        dbAdapter.open();

        long organizationId;

        // ------------------------------------------------
        // Food Institution
        // ------------------------------------------------

        if (pendingRole.equals(
                "Food Institution"
        )) {

            organizationId =
                    dbAdapter.insertFoodOrganization(
                            pendingName,
                            pendingPhone,
                            "-",
                            selectedLatitude,
                            selectedLongitude
                    );

        }

        // ------------------------------------------------
        // Charity Organization
        // ------------------------------------------------

        else {

            organizationId =
                    dbAdapter.insertCharityOrganization(
                            pendingName,
                            pendingPhone,
                            "-",
                            selectedLatitude,
                            selectedLongitude
                    );
        }

        // ------------------------------------------------
        // Organization Creation Failed
        // ------------------------------------------------

        if (organizationId == -1) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    R.string.organization_creation_failed,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Create User
        // ------------------------------------------------

        User user =
                new User(
                        0,
                        pendingName,
                        pendingEmail,
                        pendingPassword,
                        null,
                        pendingRole,
                        (int) organizationId
                );

        long userId =
                dbAdapter.insertUser(user);

        // ------------------------------------------------
        // User Creation Failed
        // ------------------------------------------------

        if (userId == -1) {

            dbAdapter.close();

            Toast.makeText(
                    this,
                    R.string.email_already_registered,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Save User Location
        // ------------------------------------------------

        dbAdapter.updateUserLocation(
                (int) userId,
                selectedLatitude,
                selectedLongitude
        );

        dbAdapter.close();

        // ------------------------------------------------
        // Registration Successful
        // ------------------------------------------------

        Toast.makeText(
                this,
                R.string.registration_successful,
                Toast.LENGTH_SHORT
        ).show();

        // Go to Login
        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        LoginActivity.class
                );

        startActivity(intent);

        finish();
    }
}