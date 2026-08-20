package com.example.savefoodapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        checkSession();
    }

    private void checkSession() {

        if (!sessionManager.isLoggedIn()) {

            Intent intent = new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();

            return;
        }

        String role = sessionManager.getUserRole();

        if (role.equals("Food Institution")) {

            Intent intent = new Intent(
                    SplashActivity.this,
                    FoodHomeActivity.class
            );

            startActivity(intent);

        } else if (role.equals("Charity Organization")) {

            Intent intent = new Intent(
                    SplashActivity.this,
                    CharityHomeActivity.class
            );

            startActivity(intent);

        } else {

            sessionManager.logout();

            Intent intent = new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
        }

        finish();
    }
}