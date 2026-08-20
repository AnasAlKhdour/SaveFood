package com.example.savefoodapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.savefoodapp.database.DBAdapter;
import com.example.savefoodapp.models.FoodDonation;
import com.example.savefoodapp.models.User;
import com.example.savefoodapp.utils.SessionManager;

import java.util.List;

public class MyOffersActivity extends AppCompatActivity {

    private LinearLayout offersContainer;
    private TextView tvNoOffers;

    private DBAdapter dbAdapter;
    private SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_offers);

        offersContainer = findViewById(R.id.offersContainer);
        tvNoOffers = findViewById(R.id.tvNoOffers);

        dbAdapter = new DBAdapter(this);
        sessionManager = new SessionManager(this);

        loadOffers();
    }

    private void loadOffers() {

        String userEmail = sessionManager.getUserEmail();

        if (userEmail == null || userEmail.isEmpty()) {
            tvNoOffers.setVisibility(View.VISIBLE);
            tvNoOffers.setText("User session not found");
            return;
        }

        dbAdapter.open();

        User user = dbAdapter.getUser(userEmail);

        if (user == null) {
            dbAdapter.close();

            tvNoOffers.setVisibility(View.VISIBLE);
            tvNoOffers.setText("User not found");
            return;
        }

        int organizationId = user.getOrganizationId();

        if (organizationId <= 0) {
            dbAdapter.close();

            tvNoOffers.setVisibility(View.VISIBLE);
            tvNoOffers.setText("Food organization not found");
            return;
        }

        List<FoodDonation> offers =
                dbAdapter.getFoodDonationsByOrganizationId(organizationId);

        dbAdapter.close();

        if (offers.isEmpty()) {
            tvNoOffers.setVisibility(View.VISIBLE);
            tvNoOffers.setText("You have no offers yet");
            return;
        }

        tvNoOffers.setVisibility(View.GONE);

        for (FoodDonation offer : offers) {
            addOfferView(offer);
        }
    }

    private void addOfferView(FoodDonation offer) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 16);

        card.setLayoutParams(cardParams);

        TextView foodName = new TextView(this);
        foodName.setText(offer.getFoodName());
        foodName.setTextSize(20);
        foodName.setTextColor(0xFF333333);
        foodName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView quantity = new TextView(this);
        quantity.setText("Quantity: " + offer.getQuantity());
        quantity.setTextSize(16);

        TextView description = new TextView(this);
        description.setText("Description: " + offer.getDescription());
        description.setTextSize(16);

        TextView expiryDate = new TextView(this);
        expiryDate.setText("Expiry Date: " + offer.getExpiryDate());
        expiryDate.setTextSize(16);

        TextView status = new TextView(this);
        status.setText("Status: " + offer.getStatus());
        status.setTextSize(16);

        // Edit Button
        Button btnEdit = new Button(this);
        btnEdit.setText("Edit");

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                android.content.Intent intent =
                        new android.content.Intent(
                                MyOffersActivity.this,
                                EditOfferActivity.class
                        );

                intent.putExtra("OFFER_ID", offer.getId());

                startActivity(intent);
            }
        });

        // Delete Button
        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new androidx.appcompat.app.AlertDialog.Builder(
                        MyOffersActivity.this
                )
                        .setTitle("Delete Offer")
                        .setMessage("Are you sure you want to delete this offer?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteOffer(offer.getId());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        card.addView(foodName);
        card.addView(quantity);
        card.addView(description);
        card.addView(expiryDate);
        card.addView(status);
        card.addView(btnEdit);
        card.addView(btnDelete);

        offersContainer.addView(card);
    }

    private void deleteOffer(int offerId) {

        dbAdapter.open();

        int result = dbAdapter.deleteFoodDonation(offerId);

        dbAdapter.close();

        if (result > 0) {

            Toast.makeText(
                    this,
                    "Offer deleted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            recreate();

        } else {

            Toast.makeText(
                    this,
                    "Failed to delete offer",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

}