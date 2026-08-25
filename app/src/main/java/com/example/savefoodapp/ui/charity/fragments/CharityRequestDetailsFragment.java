package com.example.savefoodapp.ui.charity.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.DonationRequest;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.DonationRequestRepository;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.data.repository.UserRepository;

import java.util.List;

public class CharityRequestDetailsFragment
        extends Fragment {

    // ====================================================
    // UI
    // ====================================================

    private TextView tvRequestId;
    private TextView tvFoodName;
    private TextView tvAvailableQuantity;
    private TextView tvRequestedQuantity;
    private TextView tvDescription;
    private TextView tvExpiryDate;
    private TextView tvStatus;

    private Button btnEditRequest;
    private Button btnCancelRequest;
    private Button btnBack;

    // ====================================================
    // Repositories
    // ====================================================

    private UserRepository userRepository;
    private DonationRequestRepository donationRequestRepository;
    private FoodDonationRepository foodDonationRepository;

    // ====================================================
    // Session
    // ====================================================

    private SessionManager sessionManager;

    // ====================================================
    // Request
    // ====================================================

    private int requestId = -1;

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
                R.layout.fragment_charity_request_details,
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

        tvRequestId =
                view.findViewById(
                        R.id.tvRequestId
                );

        tvFoodName =
                view.findViewById(
                        R.id.tvFoodName
                );

        tvAvailableQuantity =
                view.findViewById(
                        R.id.tvAvailableQuantity
                );

        tvRequestedQuantity =
                view.findViewById(
                        R.id.tvRequestedQuantity
                );

        tvDescription =
                view.findViewById(
                        R.id.tvDescription
                );

        tvExpiryDate =
                view.findViewById(
                        R.id.tvExpiryDate
                );

        tvStatus =
                view.findViewById(
                        R.id.tvStatus
                );

        btnEditRequest =
                view.findViewById(
                        R.id.btnEditRequest
                );

        btnCancelRequest =
                view.findViewById(
                        R.id.btnCancelRequest
                );

        btnBack =
                view.findViewById(
                        R.id.btnBack
                );

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        // ------------------------------------------------
        // Get Request ID
        // ------------------------------------------------

        Bundle arguments =
                getArguments();

        if (arguments != null) {

            requestId =
                    arguments.getInt(
                            "REQUEST_ID",
                            -1
                    );
        }

        if (requestId <= 0) {

            showError(
                    "Invalid request"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Repositories
        // ------------------------------------------------

        userRepository =
                new UserRepository(
                        requireContext()
                );

        donationRequestRepository =
                new DonationRequestRepository(
                        requireContext()
                );

        foodDonationRepository =
                new FoodDonationRepository(
                        requireContext()
                );

        userRepository.open();
        donationRequestRepository.open();
        foodDonationRepository.open();

        // ------------------------------------------------
        // Load Details
        // ------------------------------------------------

        loadRequestDetails();

        // ------------------------------------------------
        // Back
        // ------------------------------------------------

        btnBack.setOnClickListener(
                view1 ->
                        goBack()
        );

        // ------------------------------------------------
        // Edit Request
        // ------------------------------------------------

        btnEditRequest.setOnClickListener(
                view1 ->
                        showEditQuantityDialog()
        );

        // ------------------------------------------------
        // Cancel Request
        // ------------------------------------------------

        btnCancelRequest.setOnClickListener(
                view1 ->
                        showCancelConfirmation()
        );
    }

    // ====================================================
    // LOAD REQUEST DETAILS
    // ====================================================

    private void loadRequestDetails() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(
                email
        )) {

            showError(
                    "User session not found"
            );

            goBack();

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

            showError(
                    "Unable to load user"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Charity Organization".equals(
                user.getRole()
        )) {

            showError(
                    "Invalid account type"
            );

            goBack();

            return;
        }

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            showError(
                    "Charity organization not found"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Load Request
        // ------------------------------------------------

        DonationRequest request =
                getRequestById(
                        charityOrganizationId
                );

        if (request == null) {

            showError(
                    "Request not found"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Load Donation
        // ------------------------------------------------

        FoodDonation donation =
                foodDonationRepository
                        .getFoodDonationById(
                                request.getDonationId()
                        );

        if (donation == null) {

            showError(
                    "Food offer not found"
            );

            goBack();

            return;
        }

        // ------------------------------------------------
        // Display Data
        // ------------------------------------------------

        tvRequestId.setText(
                "#" + request.getId()
        );

        tvFoodName.setText(
                donation.getFoodName()
        );

        tvAvailableQuantity.setText(
                donation.getQuantity() + " units"
        );

        tvRequestedQuantity.setText(
                request.getQuantityRequested() + " units"
        );

        String description = donation.getDescription();

        if (TextUtils.isEmpty(description)) {
            tvDescription.setText("No description provided");
        } else {
            tvDescription.setText(description);
        }

        tvExpiryDate.setText(
                donation.getExpiryDate()
        );

        tvStatus.setText(
                request.getStatus()
        );

        // ------------------------------------------------
        // Action Buttons
        // ------------------------------------------------

        boolean pending =
                "PENDING".equalsIgnoreCase(
                        request.getStatus()
                );

        if (pending) {

            btnEditRequest.setVisibility(
                    View.VISIBLE
            );

            btnCancelRequest.setVisibility(
                    View.VISIBLE
            );

            btnEditRequest.setEnabled(
                    true
            );

            btnCancelRequest.setEnabled(
                    true
            );

        } else {

            btnEditRequest.setVisibility(
                    View.GONE
            );

            btnCancelRequest.setVisibility(
                    View.GONE
            );
        }
    }

    // ====================================================
    // GET REQUEST BY ID
    // ====================================================

    private DonationRequest getRequestById(
            int charityOrganizationId
    ) {

        List<DonationRequest> requests =
                donationRequestRepository
                        .getRequestsByCharity(
                                charityOrganizationId
                        );

        if (requests == null) {

            return null;
        }

        for (DonationRequest request :
                requests) {

            if (request.getId()
                    == requestId) {

                return request;
            }
        }

        return null;
    }

    // ====================================================
    // EDIT REQUEST QUANTITY DIALOG
    // ====================================================

    private void showEditQuantityDialog() {

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(email)) {

            showError(
                    "User session not found"
            );

            return;
        }

        User user =
                userRepository.getUser(email);

        if (user == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            showError(
                    "Charity organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Request
        // ------------------------------------------------

        DonationRequest request =
                getRequestById(
                        charityOrganizationId
                );

        if (request == null) {

            showError(
                    "Request not found"
            );

            return;
        }

        // ------------------------------------------------
        // Pending Only
        // ------------------------------------------------

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus()
        )) {

            showError(
                    "Only pending requests can be edited"
            );

            loadRequestDetails();

            return;
        }

        // ------------------------------------------------
        // Get Donation
        // ------------------------------------------------

        FoodDonation donation =
                foodDonationRepository
                        .getFoodDonationById(
                                request.getDonationId()
                        );

        if (donation == null) {

            showError(
                    "Food offer not found"
            );

            return;
        }

        // ------------------------------------------------
        // Input
        // ------------------------------------------------

        EditText input =
                new EditText(
                        requireContext()
                );

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setSingleLine(true);

        input.setText(
                String.valueOf(
                        request.getQuantityRequested()
                )
        );

        input.selectAll();

        input.setTextSize(18);

        input.setTextColor(
                requireContext()
                        .getColor(
                                R.color.savefood_text_primary
                        )
        );

        input.setPadding(
                8,
                8,
                8,
                8
        );

        // Green underline / accent
        input.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        requireContext().getColor(
                                R.color.savefood_green
                        )
                )
        );

        // ------------------------------------------------
        // Dialog
        // ------------------------------------------------

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle(
                                "Edit Requested Quantity"
                        )
                        .setMessage(
                                "Available quantity: "
                                        + donation.getQuantity()
                        )
                        .setView(
                                input,
                                24,
                                0,
                                24,
                                0
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();

        // ------------------------------------------------
        // Show Dialog
        // ------------------------------------------------

        dialog.setOnShowListener(
                dialogInterface -> {

                    // ----------------------------------------
                    // Dialog Window
                    // ----------------------------------------

                    if (dialog.getWindow() != null) {

                        android.graphics.drawable.GradientDrawable
                                background =
                                new android.graphics.drawable.GradientDrawable();

                        background.setColor(
                                requireContext().getColor(
                                        R.color.savefood_surface
                                )
                        );

                        background.setCornerRadius(
                                24f
                        );

                        dialog.getWindow()
                                .setBackgroundDrawable(
                                        background
                                );
                    }

                    // ----------------------------------------
                    // Title
                    // ----------------------------------------

                    int titleId =
                            getResources()
                                    .getIdentifier(
                                            "alertTitle",
                                            "id",
                                            requireContext()
                                                    .getPackageName()
                                    );

                    TextView title =
                            dialog.findViewById(
                                    titleId
                            );

                    if (title != null) {

                        title.setTextColor(
                                requireContext().getColor(
                                        R.color.savefood_text_primary
                                )
                        );

                        title.setTextSize(
                                22
                        );

                        title.setTypeface(
                                title.getTypeface(),
                                android.graphics.Typeface.BOLD
                        );
                    }

                    // ----------------------------------------
                    // Message
                    // ----------------------------------------

                    TextView message =
                            dialog.findViewById(
                                    android.R.id.message
                            );

                    if (message != null) {

                        message.setTextColor(
                                requireContext().getColor(
                                        R.color.savefood_text_secondary
                                )
                        );

                        message.setTextSize(
                                16
                        );
                    }

                    // ----------------------------------------
                    // Cancel Button
                    // ----------------------------------------

                    Button cancelButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_NEGATIVE
                            );

                    if (cancelButton != null) {

                        cancelButton.setTextColor(
                                requireContext().getColor(
                                        R.color.savefood_green
                                )
                        );
                    }

                    // ----------------------------------------
                    // Save Button
                    // ----------------------------------------

                    Button saveButton =
                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            );

                    if (saveButton != null) {

                        saveButton.setTextColor(
                                requireContext().getColor(
                                        R.color.savefood_green
                                )
                        );

                        saveButton.setOnClickListener(
                                view -> {

                                    String quantityText =
                                            input.getText()
                                                    .toString()
                                                    .trim();

                                    // --------------------------------
                                    // Empty
                                    // --------------------------------

                                    if (TextUtils.isEmpty(
                                            quantityText
                                    )) {

                                        input.setError(
                                                "Enter requested quantity"
                                        );

                                        return;
                                    }

                                    int newQuantity;

                                    try {

                                        newQuantity =
                                                Integer.parseInt(
                                                        quantityText
                                                );

                                    } catch (
                                            NumberFormatException e
                                    ) {

                                        input.setError(
                                                "Enter a valid quantity"
                                        );

                                        return;
                                    }

                                    // --------------------------------
                                    // Positive
                                    // --------------------------------

                                    if (newQuantity <= 0) {

                                        input.setError(
                                                "Quantity must be greater than 0"
                                        );

                                        return;
                                    }

                                    // --------------------------------
                                    // Available Quantity
                                    // --------------------------------

                                    if (newQuantity
                                            > donation.getQuantity()) {

                                        input.setError(
                                                "Requested quantity cannot exceed available quantity"
                                        );

                                        return;
                                    }

                                    // --------------------------------
                                    // Same Quantity
                                    // --------------------------------

                                    if (newQuantity
                                            == request.getQuantityRequested()) {

                                        dialog.dismiss();

                                        return;
                                    }

                                    // --------------------------------
                                    // Update
                                    // --------------------------------

                                    boolean success =
                                            donationRequestRepository
                                                    .updateRequestQuantity(
                                                            requestId,
                                                            charityOrganizationId,
                                                            newQuantity
                                                    );

                                    if (success) {

                                        Toast.makeText(
                                                requireContext(),
                                                "Request updated successfully",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        dialog.dismiss();

                                        loadRequestDetails();

                                    } else {

                                        Toast.makeText(
                                                requireContext(),
                                                "Unable to update request",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    }
                }
        );

        dialog.show();
    }

    // ====================================================
    // CANCEL CONFIRMATION
    // ====================================================

    private void showCancelConfirmation() {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Cancel Request"
                )
                .setMessage(
                        "Are you sure you want to cancel this request?"
                )
                .setNegativeButton(
                        "No",
                        null
                )
                .setPositiveButton(
                        "Yes",
                        (dialog, which) ->
                                cancelRequest()
                )
                .show();
    }

    // ====================================================
    // CANCEL REQUEST
    // ====================================================

    private void cancelRequest() {

        String email =
                sessionManager.getUserEmail();

        if (TextUtils.isEmpty(
                email
        )) {

            showError(
                    "User session not found"
            );

            return;
        }

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            showError(
                    "Unable to load user"
            );

            return;
        }

        // ------------------------------------------------
        // Validate Role
        // ------------------------------------------------

        if (!"Charity Organization".equals(
                user.getRole()
        )) {

            showError(
                    "Invalid account type"
            );

            return;
        }

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            showError(
                    "Charity organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Request Again
        // ------------------------------------------------

        DonationRequest request =
                getRequestById(
                        charityOrganizationId
                );

        if (request == null) {

            showError(
                    "Request not found"
            );

            return;
        }

        // ------------------------------------------------
        // Must Be Pending
        // ------------------------------------------------

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus()
        )) {

            showError(
                    "Only pending requests can be cancelled"
            );

            loadRequestDetails();

            return;
        }

        // ------------------------------------------------
        // Cancel
        // ------------------------------------------------

        boolean success =
                donationRequestRepository
                        .cancelRequest(
                                requestId,
                                charityOrganizationId
                        );

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (success) {

            Toast.makeText(
                    requireContext(),
                    "Request cancelled successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Unable to cancel request",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();
        }
    }

    // ====================================================
    // BACK
    // ====================================================

    private void goBack() {

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    // ====================================================
    // ERROR
    // ====================================================

    private void showError(
            String message
    ) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
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

        if (foodDonationRepository != null) {

            foodDonationRepository.close();
            foodDonationRepository = null;
        }

        super.onDestroyView();
    }
}