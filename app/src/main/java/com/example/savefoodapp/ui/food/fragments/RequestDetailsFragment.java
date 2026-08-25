package com.example.savefoodapp.ui.food.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class RequestDetailsFragment extends Fragment {

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

    private Button btnAccept;
    private Button btnReject;
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
                R.layout.fragment_request_details,
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

        btnAccept =
                view.findViewById(
                        R.id.btnAccept
                );

        btnReject =
                view.findViewById(
                        R.id.btnReject
                );

        btnBack =
                view.findViewById(
                        R.id.btnBack
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

            Toast.makeText(
                    requireContext(),
                    "Invalid request",
                    Toast.LENGTH_SHORT
            ).show();

            goBack();

            return;
        }

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
        // Accept
        // ------------------------------------------------

        btnAccept.setOnClickListener(
                view1 ->
                        acceptRequest()
        );

        // ------------------------------------------------
        // Reject
        // ------------------------------------------------

        btnReject.setOnClickListener(
                view1 ->
                        rejectRequest()
        );

        updateToolbar(
                "Request Details"
        );
    }

    // ====================================================
    // LOAD REQUEST DETAILS
    // ====================================================

    private void loadRequestDetails() {

        DonationRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            Toast.makeText(
                    requireContext(),
                    "Request not found",
                    Toast.LENGTH_SHORT
            ).show();

            goBack();

            return;
        }

        // ------------------------------------------------
        // Get Related Donation
        // ------------------------------------------------

        FoodDonation donation =
                foodDonationRepository
                        .getFoodDonationById(
                                request.getDonationId()
                        );

        if (donation == null) {

            Toast.makeText(
                    requireContext(),
                    "Food offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            goBack();

            return;
        }

        // ------------------------------------------------
        // Display Information
        // ------------------------------------------------

        tvRequestId.setText(
                "Request ID: "
                        + request.getId()
        );

        tvFoodName.setText(
                "Food: "
                        + donation.getFoodName()
        );

        tvAvailableQuantity.setText(
                "Available Quantity: "
                        + donation.getQuantity()
        );

        tvRequestedQuantity.setText(
                "Requested Quantity: "
                        + request.getQuantityRequested()
        );

        tvDescription.setText(
                "Description: "
                        + donation.getDescription()
        );

        tvExpiryDate.setText(
                "Expiry Date: "
                        + donation.getExpiryDate()
        );

        tvStatus.setText(
                "Status: "
                        + request.getStatus()
        );

        // ------------------------------------------------
        // Buttons
        // ------------------------------------------------

        boolean pending =
                "PENDING".equalsIgnoreCase(
                        request.getStatus()
                );

        btnAccept.setEnabled(
                pending
        );

        btnReject.setEnabled(
                pending
        );
    }

    // ====================================================
    // GET REQUEST BY ID
    // ====================================================

    private DonationRequest getRequestById(
            int requestedId
    ) {

        String email =
                sessionManager.getUserEmail();

        if (email == null
                || email.isEmpty()) {

            return null;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null) {

            return null;
        }

        // ------------------------------------------------
        // Validate Food Institution
        // ------------------------------------------------

        if (!"Food Institution".equals(
                user.getRole()
        )) {

            return null;
        }

        int organizationId =
                user.getOrganizationId();

        if (organizationId <= 0) {

            return null;
        }

        // ------------------------------------------------
        // Get Institution Requests
        // ------------------------------------------------

        List<DonationRequest> requests =
                donationRequestRepository
                        .getRequestsByInstitution(
                                organizationId
                        );

        if (requests == null) {

            return null;
        }

        // ------------------------------------------------
        // Find Request
        // ------------------------------------------------

        for (DonationRequest request :
                requests) {

            if (request.getId()
                    == requestedId) {

                return request;
            }
        }

        return null;
    }

    // ====================================================
    // ACCEPT REQUEST
    // ====================================================

    private void acceptRequest() {

        DonationRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            Toast.makeText(
                    requireContext(),
                    "Request not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Validate Pending
        // ------------------------------------------------

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus()
        )) {

            Toast.makeText(
                    requireContext(),
                    "This request has already been processed",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

            return;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null
                || user.getOrganizationId() <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Food organization not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Accept
        // ------------------------------------------------

        boolean success =
                donationRequestRepository
                        .acceptRequest(
                                requestId,
                                user.getOrganizationId()
                        );

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (success) {

            Toast.makeText(
                    requireContext(),
                    "Request accepted successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Unable to accept request. "
                            + "Requested quantity may exceed available quantity.",
                    Toast.LENGTH_LONG
            ).show();

            loadRequestDetails();
        }
    }

    // ====================================================
    // REJECT REQUEST
    // ====================================================

    private void rejectRequest() {

        DonationRequest request =
                getRequestById(
                        requestId
                );

        if (request == null) {

            Toast.makeText(
                    requireContext(),
                    "Request not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Validate Pending
        // ------------------------------------------------

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus()
        )) {

            Toast.makeText(
                    requireContext(),
                    "This request has already been processed",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

            return;
        }

        // ------------------------------------------------
        // Get Current User
        // ------------------------------------------------

        String email =
                sessionManager.getUserEmail();

        User user =
                userRepository.getUser(
                        email
                );

        if (user == null
                || user.getOrganizationId() <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Food organization not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // Reject
        // ------------------------------------------------

        int rowsUpdated =
                donationRequestRepository
                        .updateRequestStatus(
                                requestId,
                                user.getOrganizationId(),
                                "REJECTED"
                        );

        boolean success =
                rowsUpdated > 0;

        // ------------------------------------------------
        // Result
        // ------------------------------------------------

        if (success) {

            Toast.makeText(
                    requireContext(),
                    "Request rejected successfully",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();

        } else {

            Toast.makeText(
                    requireContext(),
                    "Unable to reject request",
                    Toast.LENGTH_SHORT
            ).show();

            loadRequestDetails();
        }
    }

    // ====================================================
    // GO BACK
    // ====================================================

    private void goBack() {

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
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