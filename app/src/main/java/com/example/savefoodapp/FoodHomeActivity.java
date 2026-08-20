package com.example.savefoodapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.savefoodapp.utils.SessionManager;

import androidx.appcompat.app.AppCompatActivity;

public class FoodHomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnAddOffer;
    private Button btnMyOffers;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnAddOffer = findViewById(R.id.btnAddOffer);
        btnMyOffers = findViewById(R.id.btnMyOffers);
        btnLogout = findViewById(R.id.btnLogout);

        // Get the logged-in user's name
        String userName = getIntent().getStringExtra("USER_NAME");

        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome, " + userName);
        }

        // Add Food Offer
        btnAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        FoodHomeActivity.this,
                        AddOfferActivity.class
                );

                startActivity(intent);
            }
        });

        // My Offers
        btnMyOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        FoodHomeActivity.this,
                        "My Offers",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout() {

        SessionManager sessionManager =
                new SessionManager(this);

        sessionManager.logout();

        Intent intent = new Intent(
                FoodHomeActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}