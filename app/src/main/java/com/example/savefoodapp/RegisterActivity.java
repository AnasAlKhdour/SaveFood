package com.example.savefoodapp;

import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private RadioGroup rgRole;
    private RadioButton rbFoodInstitution;
    private RadioButton rbCharity;

    private Button btnRegister;

    private DBAdapter dbAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        rgRole = findViewById(R.id.rgRole);
        rbFoodInstitution = findViewById(R.id.rbFoodInstitution);
        rbCharity = findViewById(R.id.rbCharity);

        btnRegister = findViewById(R.id.btnRegister);

        dbAdapter = new DBAdapter(this);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Name validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your name");
            etName.requestFocus();
            return;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Role validation
        if (rgRole.getCheckedRadioButtonId() == -1) {
            Toast.makeText(
                    RegisterActivity.this,
                    "Please select your role",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String role;

        if (rbFoodInstitution.isChecked()) {
            role = "Food Institution";
        } else {
            role = "Charity Organization";
        }
        // Open database
        dbAdapter.open();

// Create User object
        User user = new User(
                0,
                name,
                email,
                password,
                null,
                role,
                0
        );
// Insert user
        long userId = dbAdapter.insertUser(user);
        dbAdapter.close();
        if (userId == -1) {


            etEmail.setError("This email is already registered");
            etEmail.requestFocus();

            return;

        } else {

            Toast.makeText(
                    RegisterActivity.this,
                    "Registration successful",
                    Toast.LENGTH_SHORT
            ).show();

            // Go to Login
            Intent intent = new Intent(
                    RegisterActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

            finish();
        }
    }