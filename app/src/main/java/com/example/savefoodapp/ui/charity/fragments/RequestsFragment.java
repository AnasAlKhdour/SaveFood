package com.example.savefoodapp.ui.charity.fragments;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.DonationRequest;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.DonationRequestRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.example.savefoodapp.ui.charity.adapters.RequestAdapter;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment
        extends Fragment {

    private RecyclerView recyclerViewRequests;

    private View layoutEmptyRequests;

    private UserRepository userRepository;
    private DonationRequestRepository donationRequestRepository;

    private SessionManager sessionManager;

    private RequestAdapter requestAdapter;

    private List<DonationRequest> requests;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_requests,
                container,
                false
        );
    }

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
        // Initialize UI
        // ------------------------------------------------

        recyclerViewRequests =
                view.findViewById(
                        R.id.recyclerViewRequests
                );

        layoutEmptyRequests =
                view.findViewById(
                        R.id.layoutEmptyRequests
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
        // RecyclerView
        // ------------------------------------------------

        recyclerViewRequests.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        requestAdapter =
                new RequestAdapter(
                        new ArrayList<>()
                );

        recyclerViewRequests.setAdapter(
                requestAdapter
        );

        // ------------------------------------------------
        // Request Click Listener
        // ------------------------------------------------

        setupRequestClickListener();

        // ------------------------------------------------
        // Load Requests
        // ------------------------------------------------

        loadMyRequests();
    }

    // ====================================================
    // REQUEST CLICK LISTENER
    // ====================================================

    private void setupRequestClickListener() {

        final GestureDetector gestureDetector =
                new GestureDetector(
                        requireContext(),
                        new GestureDetector
                                .SimpleOnGestureListener() {

                            @Override
                            public boolean onSingleTapUp(
                                    MotionEvent e
                            ) {

                                View child =
                                        recyclerViewRequests
                                                .findChildViewUnder(
                                                        e.getX(),
                                                        e.getY()
                                                );

                                if (child == null) {

                                    return false;
                                }

                                int position =
                                        recyclerViewRequests
                                                .getChildAdapterPosition(
                                                        child
                                                );

                                if (position ==
                                        RecyclerView.NO_POSITION) {

                                    return false;
                                }

                                if (requests == null
                                        || position >= requests.size()) {

                                    return false;
                                }

                                DonationRequest request =
                                        requests.get(
                                                position
                                        );

                                openRequestDetails(
                                        request.getId()
                                );

                                return true;
                            }
                        }
                );

        recyclerViewRequests.addOnItemTouchListener(
                new RecyclerView.SimpleOnItemTouchListener() {

                    @Override
                    public boolean onInterceptTouchEvent(
                            @NonNull RecyclerView recyclerView,
                            @NonNull MotionEvent event
                    ) {

                        return gestureDetector.onTouchEvent(
                                event
                        );
                    }
                }
        );
    }

    // ====================================================
    // LOAD MY REQUESTS
    // ====================================================

    private void loadMyRequests() {

        String email =
                sessionManager.getUserEmail();

        // ------------------------------------------------
        // Validate Session
        // ------------------------------------------------

        if (email == null
                || email.isEmpty()) {

            showError(
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
                    "Only charity organizations can view requests"
            );

            return;
        }

        // ------------------------------------------------
        // Organization ID
        // ------------------------------------------------

        int charityOrganizationId =
                user.getOrganizationId();

        if (charityOrganizationId <= 0) {

            showError(
                    "Charity organization not found"
            );

            return;
        }

        // ------------------------------------------------
        // Get Requests
        // ------------------------------------------------

        requests =
                donationRequestRepository
                        .getRequestsByCharity(
                                charityOrganizationId
                        );

        if (requests == null) {

            requests =
                    new ArrayList<>();
        }

        requestAdapter.updateRequests(
                requests
        );

        updateEmptyState();
    }

    // ====================================================
    // OPEN REQUEST DETAILS
    // ====================================================

    private void openRequestDetails(
            int requestId
    ) {

        CharityRequestDetailsFragment fragment =
                new CharityRequestDetailsFragment();

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
                        "charity_request_details"
                )
                .commit();

        // ------------------------------------------------
        // Toolbar
        // ------------------------------------------------

        if (requireActivity()
                instanceof CharityMainActivity) {

            ((CharityMainActivity)
                    requireActivity())
                    .setProfileToolbarTitle(
                            "Request Details"
                    );
        }
    }

    // ====================================================
    // EMPTY STATE
    // ====================================================

    private void updateEmptyState() {

        boolean empty =
                requests == null
                        || requests.isEmpty();

        if (empty) {

            recyclerViewRequests.setVisibility(
                    View.GONE
            );

            layoutEmptyRequests.setVisibility(
                    View.VISIBLE
            );

        } else {

            recyclerViewRequests.setVisibility(
                    View.VISIBLE
            );

            layoutEmptyRequests.setVisibility(
                    View.GONE
            );
        }
    }

    // ====================================================
    // REFRESH
    // ====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (userRepository != null
                && donationRequestRepository != null
                && requestAdapter != null) {

            loadMyRequests();
        }
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

        requests =
                new ArrayList<>();

        if (requestAdapter != null) {

            requestAdapter.updateRequests(
                    requests
            );
        }

        updateEmptyState();
    }

    // ====================================================
    // DATABASE LIFECYCLE
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