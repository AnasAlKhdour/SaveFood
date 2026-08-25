package com.example.savefoodapp.ui.food.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.DonationRequest;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.DonationRequestRepository;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.food.FoodMainActivity;

import java.util.List;

public class RequestsFragment extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private LinearLayout requestsContainer;
    private TextView tvNoRequests;

    // ====================================================
    // Repositories
    // ====================================================

    private DonationRequestRepository donationRequestRepository;
    private FoodDonationRepository foodDonationRepository;
    private UserRepository userRepository;

    // ====================================================
    // Session
    // ====================================================

    private SessionManager sessionManager;

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
                R.layout.fragment_food_requests,
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

        requestsContainer =
                view.findViewById(
                        R.id.requestsContainer
                );

        tvNoRequests =
                view.findViewById(
                        R.id.tvNoRequests
                );

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Repositories
        // ------------------------------------------------

        donationRequestRepository =
                new DonationRequestRepository(
                        requireContext()
                );

        foodDonationRepository =
                new FoodDonationRepository(
                        requireContext()
                );

        userRepository =
                new UserRepository(
                        requireContext()
                );

        donationRequestRepository.open();
        foodDonationRepository.open();
        userRepository.open();

        // ------------------------------------------------
        // Load Requests
        // ------------------------------------------------

        loadRequests();

        updateToolbar(
                "Requests"
        );
    }

    // ====================================================
    // REFRESH WHEN RETURNING
    // ====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (requestsContainer != null
                && sessionManager != null) {

            loadRequests();
        }

        updateToolbar(
                "Requests"
        );
    }

    // ====================================================
    // LOAD REQUESTS
    // ====================================================

    private void loadRequests() {

        requestsContainer.removeAllViews();

        tvNoRequests.setVisibility(
                View.GONE
        );

        String email =
                sessionManager.getUserEmail();

        if (email == null
                || email.isEmpty()) {

            showEmptyMessage(
                    "User session not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            showEmptyMessage(
                    "User not found"
            );

            return;
        }

        // ------------------------------------------------
        // Validate Food Institution
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
        // Organization ID
        // ------------------------------------------------

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            showEmptyMessage(
                    "Food organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Requests
        // ------------------------------------------------

        List<DonationRequest> requests =
                donationRequestRepository
                        .getRequestsByInstitution(
                                organizationId
                        );

        if (requests == null
                || requests.isEmpty()) {

            showEmptyMessage(
                    "No incoming requests"
            );

            return;
        }

        // ------------------------------------------------
        // Display Requests
        // ------------------------------------------------

        tvNoRequests.setVisibility(
                View.GONE
        );

        for (DonationRequest request :
                requests) {

            addRequestView(
                    request
            );
        }
    }

    // ====================================================
    // ADD REQUEST VIEW
    // ====================================================

    private void addRequestView(
            DonationRequest request
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

        cardParams.setMargins(
                0,
                0,
                0,
                (int) getResources().getDimension(
                        R.dimen.spacing_12
                )
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
        // REQUEST ID
        // ====================================================

        TextView tvRequestId =
                new TextView(
                        requireContext()
                );

        tvRequestId.setText(
                "Request ID: "
                        + request.getId()
        );

        tvRequestId.setTextSize(
                getResources().getDimension(
                        R.dimen.text_card_title
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        tvRequestId.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_primary
                )
        );

        tvRequestId.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        // ====================================================
        // FOOD INFORMATION
        // ====================================================

        FoodDonation donation =
                foodDonationRepository
                        .getFoodDonationById(
                                request.getDonationId()
                        );

        TextView tvFood =
                new TextView(
                        requireContext()
                );

        if (donation != null) {

            tvFood.setText(
                    "Food: "
                            + donation.getFoodName()
            );

        } else {

            tvFood.setText(
                    "Food: unavailable"
            );
        }

        tvFood.setTextSize(
                getResources().getDimension(
                        R.dimen.text_body_small
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        tvFood.setTextColor(
                requireContext().getColor(
                        R.color.savefood_text_secondary
                )
        );

        LinearLayout.LayoutParams foodParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        foodParams.topMargin =
                (int) getResources().getDimension(
                        R.dimen.spacing_8
                );

        tvFood.setLayoutParams(
                foodParams
        );

        // ====================================================
        // REQUESTED QUANTITY
        // ====================================================

        TextView tvQuantity =
                new TextView(
                        requireContext()
                );

        tvQuantity.setText(
                "Requested Quantity: "
                        + request.getQuantityRequested()
        );

        tvQuantity.setTextSize(
                getResources().getDimension(
                        R.dimen.text_body_small
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        tvQuantity.setTextColor(
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

        tvQuantity.setLayoutParams(
                quantityParams
        );

        // ====================================================
        // STATUS
        // ====================================================

        TextView tvStatus =
                new TextView(
                        requireContext()
                );

        String requestStatus =
                request.getStatus();

        if (requestStatus == null
                || requestStatus.trim().isEmpty()) {

            requestStatus = "UNKNOWN";
        }

        tvStatus.setText(
                requestStatus
        );

        tvStatus.setTextSize(
                getResources().getDimension(
                        R.dimen.text_status
                ) / getResources().getDisplayMetrics().scaledDensity
        );

        int statusTextColor =
                requireContext().getColor(
                        R.color.savefood_warning
                );

        int statusBackgroundColor =
                requireContext().getColor(
                        R.color.savefood_warning_light
                );

        if ("ACCEPTED".equalsIgnoreCase(
                requestStatus
        )) {

            statusTextColor =
                    requireContext().getColor(
                            R.color.savefood_success
                    );

            statusBackgroundColor =
                    requireContext().getColor(
                            R.color.savefood_success_light
                    );

        } else if ("REJECTED".equalsIgnoreCase(
                requestStatus
        )) {

            statusTextColor =
                    requireContext().getColor(
                            R.color.savefood_error
                    );

            statusBackgroundColor =
                    requireContext().getColor(
                            R.color.savefood_error_light
                    );
        }

        tvStatus.setTextColor(
                statusTextColor
        );

        android.graphics.drawable.GradientDrawable
                statusBackground =
                new android.graphics.drawable.GradientDrawable();

        statusBackground.setColor(
                statusBackgroundColor
        );

        statusBackground.setCornerRadius(
                getResources().getDimension(
                        R.dimen.card_corner_radius_small
                )
        );

        tvStatus.setBackground(
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

        tvStatus.setPadding(
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

        tvStatus.setLayoutParams(
                statusParams
        );

        // ====================================================
        // OPEN DETAILS
        // ====================================================

        card.setClickable(
                true
        );

        card.setFocusable(
                true
        );

        card.setOnClickListener(
                view ->
                        openRequestDetails(
                                request.getId()
                        )
        );

        // ====================================================
        // ADD VIEWS
        // ====================================================

        content.addView(
                tvRequestId
        );

        content.addView(
                tvFood
        );

        content.addView(
                tvQuantity
        );

        content.addView(
                tvStatus
        );

        card.addView(
                content
        );

        requestsContainer.addView(
                card
        );
    }

    // ====================================================
    // OPEN REQUEST DETAILS
    // ====================================================

    private void openRequestDetails(
            int requestId
    ) {

        RequestDetailsFragment fragment =
                new RequestDetailsFragment();

        Bundle bundle =
                new Bundle();

        bundle.putInt(
                "REQUEST_ID",
                requestId
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
                        "food_request_details"
                )
                .commit();

        updateToolbar(
                "Request Details"
        );
    }

    // ====================================================
    // EMPTY STATE
    // ====================================================

    private void showEmptyMessage(
            String message
    ) {

        tvNoRequests.setVisibility(
                View.VISIBLE
        );

        tvNoRequests.setText(
                message
        );
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

        if (donationRequestRepository != null) {

            donationRequestRepository.close();
            donationRequestRepository = null;
        }

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