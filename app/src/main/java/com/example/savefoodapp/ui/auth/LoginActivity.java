package com.example.savefoodapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.example.savefoodapp.ui.food.FoodMainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private Button btnLogin;
    private TextView tvRegister;

    private SessionManager sessionManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_login
        );

        // ------------------------------------------------
        // Connect UI
        // ------------------------------------------------

        etEmail =
                findViewById(
                        R.id.etEmail
                );

        etPassword =
                findViewById(
                        R.id.etPassword
                );

        btnLogin =
                findViewById(
                        R.id.btnLogin
                );

        tvRegister =
                findViewById(
                        R.id.tvRegister
                );

        // ------------------------------------------------
        // Initialize Repository
        // ------------------------------------------------

        userRepository =
                new UserRepository(this);

        userRepository.open();

        // ------------------------------------------------
        // Initialize Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(this);

        // ------------------------------------------------
        // Login Button
        // ------------------------------------------------

        btnLogin.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        loginUser();
                    }
                }
        );

        // ------------------------------------------------
        // Register Link
        // ------------------------------------------------

        tvRegister.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Intent intent =
                                new Intent(
                                        LoginActivity.this,
                                        RegisterActivity.class
                                );

                        startActivity(intent);
                    }
                }
        );
    }

    // ====================================================
    // LOGIN
    // ====================================================

    private void loginUser() {

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        // ------------------------------------------------
        // Email Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(email)) {

            etEmail.setError(
                    "Please enter your email"
            );

            etEmail.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email"
            );

            etEmail.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Password Validation
        // ------------------------------------------------

        if (TextUtils.isEmpty(password)) {

            etPassword.setError(
                    "Please enter your password"
            );

            etPassword.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Authenticate User
        // ------------------------------------------------

        User user =
                userRepository.authenticate(
                        email,
                        password
                );

        if (user == null) {

            Toast.makeText(
                    this,
                    "Invalid email or password",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Get User Information
        // ------------------------------------------------

        int userId =
                user.getId();

        String name =
                user.getName();

        String role =
                user.getRole();

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Food Institution".equals(role)
                && !"Charity Organization".equals(role)) {

            Toast.makeText(
                    this,
                    "Invalid user role",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Create Session
        // ------------------------------------------------

        sessionManager.createSession(
                userId,
                name,
                email,
                role
        );

        Toast.makeText(
                this,
                "Welcome " + name,
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
// Role-Based Navigation
// ------------------------------------------------

        if ("Food Institution".equals(role)) {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            FoodMainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

        } else {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            CharityMainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        }

        finish();
    }

    // ====================================================
    // DATABASE LIFECYCLE
    // ====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (userRepository != null) {

            userRepository.close();
        }
    }
}