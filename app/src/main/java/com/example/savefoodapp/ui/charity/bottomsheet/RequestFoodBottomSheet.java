package com.example.savefoodapp.ui.charity.bottomsheet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.DonationRequest;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.DonationRequestRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class RequestFoodBottomSheet
        extends BottomSheetDialogFragment {

    private static final String ARG_OFFER_ID =
            "offer_id";

    private static final String ARG_FOOD_NAME =
            "food_name";

    private static final String ARG_AVAILABLE_QUANTITY =
            "available_quantity";

    private int donationId;
    private int availableQuantity;
    private String foodName;

    private TextView tvFoodName;
    private TextView tvAvailableQuantity;
    private EditText etRequestedQuantity;
    private MaterialButton btnSubmitRequest;

    private UserRepository userRepository;
    private DonationRequestRepository donationRequestRepository;

    private SessionManager sessionManager;

    public RequestFoodBottomSheet() {
        // Required empty constructor
    }

    // ====================================================
    // NEW INSTANCE
    // ====================================================

    public static RequestFoodBottomSheet newInstance(
            int donationId,
            String foodName,
            int availableQuantity
    ) {

        RequestFoodBottomSheet sheet =
                new RequestFoodBottomSheet();

        Bundle args =
                new Bundle();

        args.putInt(
                ARG_OFFER_ID,
                donationId
        );

        args.putString(
                ARG_FOOD_NAME,
                foodName
        );

        args.putInt(
                ARG_AVAILABLE_QUANTITY,
                availableQuantity
        );

        sheet.setArguments(
                args
        );

        return sheet;
    }

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
                R.layout.bottom_sheet_request_food,
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
        // Get Arguments
        // ------------------------------------------------

        if (getArguments() != null) {

            donationId =
                    getArguments().getInt(
                            ARG_OFFER_ID,
                            -1
                    );

            foodName =
                    getArguments().getString(
                            ARG_FOOD_NAME,
                            ""
                    );

            availableQuantity =
                    getArguments().getInt(
                            ARG_AVAILABLE_QUANTITY,
                            0
                    );
        }

        // ------------------------------------------------
        // Initialize Views
        // ------------------------------------------------

        tvFoodName =
                view.findViewById(
                        R.id.tvRequestFoodName
                );

        tvAvailableQuantity =
                view.findViewById(
                        R.id.tvRequestAvailableQuantity
                );

        etRequestedQuantity =
                view.findViewById(
                        R.id.etRequestedQuantity
                );

        btnSubmitRequest =
                view.findViewById(
                        R.id.btnSubmitRequest
                );

        // ------------------------------------------------
        // Display Offer Information
        // ------------------------------------------------

        tvFoodName.setText(
                foodName
        );

        tvAvailableQuantity.setText(
                "Available: "
                        + availableQuantity
                        + " units"
        );

        // ------------------------------------------------
        // Initialize Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Initialize Repositories
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        donationRequestRepository =
                new DonationRequestRepository(
                        requireContext()
                );

        userRepository.open();
        donationRequestRepository.open();

        // ------------------------------------------------
        // Submit
        // ------------------------------------------------

        btnSubmitRequest.setOnClickListener(
                v ->
                        submitRequest()
        );
    }

    // ====================================================
    // SUBMIT REQUEST
    // ====================================================

    private void submitRequest() {

        String quantityText =
                etRequestedQuantity
                        .getText()
                        .toString()
                        .trim();

        // ------------------------------------------------
        // Validate Empty
        // ------------------------------------------------

        if (TextUtils.isEmpty(
                quantityText
        )) {

            etRequestedQuantity.setError(
                    "Enter requested quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Parse Quantity
        // ------------------------------------------------

        int requestedQuantity;

        try {

            requestedQuantity =
                    Integer.parseInt(
                            quantityText
                    );

        } catch (NumberFormatException e) {

            etRequestedQuantity.setError(
                    "Enter a valid quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Validate Positive
        // ------------------------------------------------

        if (requestedQuantity <= 0) {

            etRequestedQuantity.setError(
                    "Quantity must be greater than 0"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Validate Available Quantity
        // ------------------------------------------------

        if (requestedQuantity
                > availableQuantity) {

            etRequestedQuantity.setError(
                    "Requested quantity cannot exceed available quantity"
            );

            etRequestedQuantity.requestFocus();

            return;
        }

        // ------------------------------------------------
        // Validate Donation
        // ------------------------------------------------

        if (donationId <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Invalid food offer",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            Toast.makeText(
                    requireContext(),
                    "User session not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            Toast.makeText(
                    requireContext(),
                    "Unable to load user information",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Charity Organization".equals(
                user.getRole()
        )) {

            Toast.makeText(
                    requireContext(),
                    "Only charity organizations can request food",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Get Charity Organization ID
        // ------------------------------------------------

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Charity organization not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Create Request
        // ------------------------------------------------

        DonationRequest request =
                new DonationRequest(
                        0,
                        donationId,
                        charityOrganizationId,
                        requestedQuantity,
                        "PENDING"
                );

        // ------------------------------------------------
        // Insert Request
        // ------------------------------------------------

        long requestId =
                donationRequestRepository
                        .insertRequest(
                                request,
                                user.getId()
                        );

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (requestId == -1) {

            Toast.makeText(
                    requireContext(),
                    "Failed to create request",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ------------------------------------------------
        // Success
        // ------------------------------------------------

        Toast.makeText(
                requireContext(),
                "Food request submitted successfully",
                Toast.LENGTH_SHORT
        ).show();

        // ------------------------------------------------
        // Close Bottom Sheet
        // ------------------------------------------------

        dismiss();

        // ------------------------------------------------
        // Open My Requests
        // ------------------------------------------------

        if (requireActivity()
                instanceof CharityMainActivity) {

            ((CharityMainActivity)
                    requireActivity())
                    .openMyRequests();
        }
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

        if (donationRequestRepository != null) {

            donationRequestRepository.close();
            donationRequestRepository = null;
        }

        super.onDestroyView();
    }
}