package com.example.savefoodapp.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.ui.auth.LoginActivity;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.example.savefoodapp.ui.food.FoodMainActivity;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_splash
        );

        sessionManager =
                new SessionManager(this);

        // ------------------------------------------------
        // Show Splash Screen
        // ------------------------------------------------

        new Handler(
                Looper.getMainLooper()
        ).postDelayed(
                () -> checkSession(),
                1000
        );
    }

    // ====================================================
    // CHECK SESSION
    // ====================================================

    private void checkSession() {

        // ------------------------------------------------
        // No Active Session
        // ------------------------------------------------

        if (!sessionManager.isLoggedIn()) {

            openLogin();

            return;
        }

        // ------------------------------------------------
        // Get Role
        // ------------------------------------------------

        String role =
                sessionManager.getUserRole();

        if (role == null
                || role.trim().isEmpty()) {

            sessionManager.logout();

            openLogin();

            return;
        }

        // ------------------------------------------------
        // Food Institution
        // ------------------------------------------------

        if ("Food Institution".equals(role)) {

            Intent intent =
                    new Intent(
                            SplashActivity.this,
                            FoodMainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

            return;
        }

        // ------------------------------------------------
        // Charity Organization
        // ------------------------------------------------

        if ("Charity Organization".equals(role)) {

            Intent intent =
                    new Intent(
                            SplashActivity.this,
                            CharityMainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

            return;
        }

        // ------------------------------------------------
        // Invalid Role
        // ------------------------------------------------

        sessionManager.logout();

        openLogin();
    }

    // ====================================================
    // OPEN LOGIN
    // ====================================================

    private void openLogin() {

        Intent intent =
                new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}