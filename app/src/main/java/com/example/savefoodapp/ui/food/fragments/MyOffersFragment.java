package com.example.savefoodapp.ui.food.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;

import java.util.List;

public class MyOffersFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private LinearLayout offersContainer;
    private TextView tvNoOffers;

    // ====================================================
    // Repositories
    // ====================================================

    private FoodDonationRepository foodDonationRepository;
    private UserRepository userRepository;

    // ====================================================
    // Session
    // ====================================================

    private SessionManager sessionManager;

    // ====================================================
    // Organization
    // ====================================================

    private int currentOrganizationId = -1;

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
                R.layout.fragment_my_offers,
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
        // Connect UI
        // ------------------------------------------------

        offersContainer =
                view.findViewById(
                        R.id.offersContainer
                );

        tvNoOffers =
                view.findViewById(
                        R.id.tvNoOffers
                );

        // ------------------------------------------------
        // Initialize Repositories
        // ------------------------------------------------

        foodDonationRepository =
                new FoodDonationRepository(
                        requireContext()
                );

        userRepository =
                new UserRepository(
                        requireContext()
                );

        foodDonationRepository.open();
        userRepository.open();

        // ------------------------------------------------
        // Initialize Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Initial Load
        // ------------------------------------------------

        refreshOffers();
    }

    // ====================================================
    // ON RESUME
    // ====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (offersContainer != null
                && tvNoOffers != null
                && sessionManager != null) {

            refreshOffers();
        }

        updateToolbar(
                "My Offers"
        );
    }

    // ====================================================
    // REFRESH OFFERS
    // ====================================================

    private void refreshOffers() {

        if (offersContainer == null
                || tvNoOffers == null) {

            return;
        }

        offersContainer.removeAllViews();

        tvNoOffers.setVisibility(
                View.GONE
        );

        loadOffers();
    }

    // ====================================================
    // LOAD OFFERS
    // ====================================================

    private void loadOffers() {

        String userEmail =
                sessionManager.getUserEmail();

        if (userEmail == null
                || userEmail.isEmpty()) {

            showEmptyMessage(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        userEmail
                );

        if (user == null) {

            showEmptyMessage(
                    "User not found"
            );

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Food Institution".equals(
                user.getRole()
        )) {

            showEmptyMessage(
                    "Invalid account type"
            );

            return;
        }

        // ------------------------------------------------
        // Get Organization ID
        // ------------------------------------------------

        int organizationId =
                user.getOrganizationId();

        currentOrganizationId =
                organizationId;

        if (organizationId <= 0) {

            showEmptyMessage(
                    "Food organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Offers
        // ------------------------------------------------

        List<FoodDonation> offers =
                foodDonationRepository
                        .getFoodDonationsByOrganizationId(
                                organizationId
                        );

        // ------------------------------------------------
        // Empty Result
        // ------------------------------------------------

        if (offers == null
                || offers.isEmpty()) {

            showEmptyMessage(
                    "You have no offers yet"
            );

            return;
        }

        // ------------------------------------------------
        // Display Offers
        // ------------------------------------------------

        tvNoOffers.setVisibility(
                View.GONE
        );

        for (FoodDonation offer :
                offers) {

            addOfferView(
                    offer
            );
        }
    }

    // ====================================================
    // EMPTY MESSAGE
    // ====================================================

    private void showEmptyMessage(
            String message
    ) {

        tvNoOffers.setVisibility(
                View.VISIBLE
        );

        tvNoOffers.setText(
                message
        );
    }

    // ====================================================
    // ADD OFFER VIEW
    // ====================================================

    private void addOfferView(
            FoodDonation offer
    ) {

        // ====================================================
        // CARD
        // ====================================================

        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(
                        requireContext()
                );

        card.setCardBackgroundColor(
                requireContext().getColor(
                        R.color.savefood_surface
                )
        );

        card.setRadius(
                getResources().getDimension(
                        R.dimen.card_corner_radius
                )
        );

        card.setCardElevation(
                getResources().getDimension(
                        R.dimen.card_elevation
                )
        );

        card.setStrokeWidth(
                (int) getResources().getDimension(
                        R.dimen.card_stroke_width
                )
        );

        card.setStrokeColor(
                requireContext().getColor(
                        R.color.savefood_border
                )
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        int cardMargin =
                (int) getResources().getDimension(
                        R.dimen.spacing_12
                );

        cardParams.setMargins(
                0,
                0,
                0,
                cardMargin
        );

        card.setLayoutParams(
                cardParams
        );

        // ====================================================
        // CARD CONTENT
        // ====================================================

        LinearLayout content =
                new LinearLayout(
                        requireContext()
                );

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                (int) getResources().getDimension(
                        R.dimen.card_padding_large
                );

        content.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        // ====================================================
        // FOOD NAME
        // ====================================================

        TextView foodName =
                new TextView(
                        requireContext()
                );

        foodName.setText(
                offer.getFoodName()
        );

        foodName.setTextSize(
                getResources().getDimension(
                        R.dimen.text_card_title
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        foodName.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_primary
                )
        );

        foodName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        // ====================================================
        // QUANTITY
        // ====================================================

        TextView quantity =
                new TextView(
                        requireContext()
                );

        quantity.setText(
                "Quantity: "
                        + offer.getQuantity()
        );

        quantity.setTextSize(
                getResources().getDimension(
                        R.dimen.text_body_small
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        quantity.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_secondary
                )
        );

        LinearLayout.LayoutParams quantityParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        quantityParams.topMargin =
                (int) getResources().getDimension(
                        R.dimen.spacing_8
                );

        quantity.setLayoutParams(
                quantityParams
        );

        // ====================================================
        // DESCRIPTION
        // ====================================================

        TextView description =
                new TextView(
                        requireContext()
                );

        description.setText(
                "Description: "
                        + offer.getDescription()
        );

        description.setTextSize(
                getResources().getDimension(
                        R.dimen.text_body_small
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        description.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_secondary
                )
        );

        // ====================================================
        // EXPIRY DATE
        // ====================================================

        TextView expiryDate =
                new TextView(
                        requireContext()
                );

        expiryDate.setText(
                "Expiry Date: "
                        + offer.getExpiryDate()
        );

        expiryDate.setTextSize(
                getResources().getDimension(
                        R.dimen.text_body_small
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        expiryDate.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_secondary
                )
        );

        // ====================================================
        // STATUS
        // ====================================================

        TextView status =
                new TextView(
                        requireContext()
                );

        status.setText(
                offer.getStatus()
        );

        status.setTextSize(
                getResources().getDimension(
                        R.dimen.text_status
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        status.setTextColor(
                requireContext().getColor(
                        R.color.savefood_success
                )
        );

        android.graphics.drawable.GradientDrawable statusBackground =
                new android.graphics.drawable.GradientDrawable();

        statusBackground.setColor(
                requireContext().getColor(
                        R.color.savefood_success_light
                )
        );

        statusBackground.setCornerRadius(
                getResources().getDimension(
                        R.dimen.card_corner_radius_small
                )
        );

        status.setBackground(
                statusBackground
        );

        int statusHorizontalPadding =
                (int) getResources().getDimension(
                        R.dimen.spacing_12
                );

        int statusVerticalPadding =
                (int) getResources().getDimension(
                        R.dimen.spacing_4
                );

        status.setPadding(
                statusHorizontalPadding,
                statusVerticalPadding,
                statusHorizontalPadding,
                statusVerticalPadding
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin =
                (int) getResources().getDimension(
                        R.dimen.spacing_12
                );

        status.setLayoutParams(
                statusParams
        );

        // ====================================================
        // BUTTON ROW
        // ====================================================

        LinearLayout buttonRow =
                new LinearLayout(
                        requireContext()
                );

        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttonRow.setGravity(
                android.view.Gravity.CENTER
        );

        LinearLayout.LayoutParams buttonRowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonRowParams.topMargin =
                (int) getResources().getDimension(
                        R.dimen.spacing_16
                );

        buttonRow.setLayoutParams(
                buttonRowParams
        );

        // ====================================================
        // EDIT BUTTON
        // ====================================================

        com.google.android.material.button.MaterialButton btnEdit =
                new com.google.android.material.button.MaterialButton(
                        requireContext()
                );

        btnEdit.setText(
                "Edit"
        );

        btnEdit.setTextSize(
                getResources().getDimension(
                        R.dimen.text_secondary
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        btnEdit.setTextColor(
                requireContext().getColor(
                        R.color.white
                )
        );

        btnEdit.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(
                                R.color.savefood_green
                        )
                )
        );

        btnEdit.setCornerRadius(
                (int) getResources().getDimension(
                        R.dimen.button_corner_radius
                )
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        0,
                        getResources().getDimensionPixelSize(
                                R.dimen.button_height_small
                        ),
                        1f
                );

        editParams.setMargins(
                0,
                0,
                (int) getResources().getDimension(
                        R.dimen.spacing_8
                ),
                0
        );

        btnEdit.setLayoutParams(
                editParams
        );

        btnEdit.setOnClickListener(
                view -> {

                    EditOfferFragment fragment =
                            new EditOfferFragment();

                    Bundle bundle =
                            new Bundle();

                    bundle.putInt(
                            "OFFER_ID",
                            offer.getId()
                    );

                    fragment.setArguments(
                            bundle
                    );

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(
                                    R.id.fragmentContainer,
                                    fragment
                            )
                            .addToBackStack(
                                    "food_edit_offer"
                            )
                            .commit();

                    updateToolbar(
                            "Edit Food Offer"
                    );
                }
        );

        // ====================================================
        // DELETE BUTTON
        // ====================================================

        com.google.android.material.button.MaterialButton btnDelete =
                new com.google.android.material.button.MaterialButton(
                        requireContext()
                );

        btnDelete.setText(
                "Delete"
        );

        btnDelete.setTextSize(
                getResources().getDimension(
                        R.dimen.text_secondary
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        btnDelete.setTextColor(
                requireContext().getColor(
                        R.color.white
                )
        );

        btnDelete.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(
                                R.color.savefood_error
                        )
                )
        );

        btnDelete.setCornerRadius(
                (int) getResources().getDimension(
                        R.dimen.button_corner_radius
                )
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        0,
                        getResources().getDimensionPixelSize(
                                R.dimen.button_height_small
                        ),
                        1f
                );

        btnDelete.setLayoutParams(
                deleteParams
        );

        btnDelete.setOnClickListener(
                view ->
                        showDeleteConfirmation(
                                offer.getId()
                        )
        );

        // ====================================================
        // ADD VIEWS
        // ====================================================

        content.addView(
                foodName
        );

        content.addView(
                quantity
        );

        content.addView(
                description
        );

        content.addView(
                expiryDate
        );

        content.addView(
                status
        );

        buttonRow.addView(
                btnEdit
        );

        buttonRow.addView(
                btnDelete
        );

        content.addView(
                buttonRow
        );

        card.addView(
                content
        );

        offersContainer.addView(
                card
        );
    }

    // ====================================================
    // DELETE CONFIRMATION
    // ====================================================

    private void showDeleteConfirmation(
            int offerId
    ) {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Delete Offer"
                )
                .setMessage(
                        "Are you sure you want to delete this offer?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteOffer(
                                        offerId
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    // ====================================================
    // DELETE OFFER
    // ====================================================

    private void deleteOffer(
            int offerId
    ) {

        if (currentOrganizationId <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Food organization not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int result =
                foodDonationRepository
                        .deleteFoodDonation(
                                offerId,
                                currentOrganizationId
                        );

        if (result > 0) {

            Toast.makeText(
                    requireContext(),
                    "Offer deleted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            refreshOffers();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Failed to delete offer",
                    Toast.LENGTH_SHORT
            ).show();
        }
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
    // CLEANUP
    // ====================================================

    @Override
    public void onDestroyView() {

        if (foodDonationRepository != null) {

            foodDonationRepository.close();
            foodDonationRepository = null;
        }

        if (userRepository != null) {

            userRepository.close();
            userRepository = null;
        }

        super.onDestroyView();
    }
}