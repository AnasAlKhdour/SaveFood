package com.example.savefoodapp.ui.charity.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.models.User;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.data.repository.UserRepository;
import com.example.savefoodapp.ui.charity.CharityMainActivity;
import com.example.savefoodapp.ui.charity.adapters.OfferAdapter;
import com.example.savefoodapp.utils.DistanceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OffersFragment
        extends Fragment {

    private AppCompatEditText etSearch;
    private Spinner spinnerFilter;
    private RecyclerView recyclerViewOffers;

    private View layoutEmptyOffers;

    private FoodDonationRepository foodDonationRepository;
    private UserRepository userRepository;

    private SessionManager sessionManager;

    private OfferAdapter offerAdapter;

    private List<FoodDonation> availableOffers;
    private List<FoodDonation> filteredOffers;

    private double charityLatitude;
    private double charityLongitude;

    private int currentFilter = 0;

    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_offers,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // ------------------------------------------------
        // Initialize UI
        // ------------------------------------------------

        etSearch =
                view.findViewById(
                        R.id.etSearch
                );

        spinnerFilter =
                view.findViewById(
                        R.id.spinnerFilter
                );

        recyclerViewOffers =
                view.findViewById(
                        R.id.recyclerViewOffers
                );

        layoutEmptyOffers =
                view.findViewById(
                        R.id.layoutEmptyOffers
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
        // Charity Location
        // ------------------------------------------------

        loadCharityLocation();

        // ------------------------------------------------
        // RecyclerView
        // ------------------------------------------------

        recyclerViewOffers.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        // ------------------------------------------------
        // Adapter
        // ------------------------------------------------

        offerAdapter =
                new OfferAdapter(
                        new ArrayList<>(),
                        new HashMap<>(),
                        this::openOfferDetails
                );

        recyclerViewOffers.setAdapter(
                offerAdapter
        );

        // ------------------------------------------------
        // Filter
        // ------------------------------------------------

        setupFilter();

        // ------------------------------------------------
        // Search
        // ------------------------------------------------

        setupSearch();

        // ------------------------------------------------
        // Load Offers
        // ------------------------------------------------

        loadAvailableOffers();
    }

    // ====================================================
    // LOAD CHARITY LOCATION
    // ====================================================

    private void loadCharityLocation() {

        String email =
                sessionManager.getUserEmail();

        if (email == null
                || email.isEmpty()) {

            charityLatitude = 0.0;
            charityLongitude = 0.0;

            return;
        }

        User charityUser =
                userRepository.getUser(
                        email
                );

        if (charityUser == null) {

            charityLatitude = 0.0;
            charityLongitude = 0.0;

            return;
        }

        charityLatitude =
                charityUser.getLatitude();

        charityLongitude =
                charityUser.getLongitude();
    }

    // ====================================================
    // LOAD AVAILABLE OFFERS
    // ====================================================

    private void loadAvailableOffers() {

        availableOffers =
                foodDonationRepository
                        .getAvailableOffers();

        if (availableOffers == null) {

            availableOffers =
                    new ArrayList<>();
        }

        applySearchAndFilter();
    }

    // ====================================================
    // SEARCH
    // ====================================================

    private void setupSearch() {

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        currentSearch =
                                s.toString();

                        applySearchAndFilter();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // ====================================================
    // FILTER
    // ====================================================

    private void setupFilter() {

        String[] filterOptions = {
                "All Offers",
                "Quantity ≤ 5",
                "Quantity > 5"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                filterOptions
        );

        adapter.setDropDownViewResource(
                R.layout.spinner_dropdown_item
        );

        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setSelection(0);

        spinnerFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        currentFilter = position;
                        applySearchAndFilter();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    // ====================================================
    // SEARCH + FILTER
    // ====================================================

    private void applySearchAndFilter() {

        if (availableOffers == null) {
            return;
        }

        filteredOffers =
                new ArrayList<>();

        String searchText =
                currentSearch
                        .toLowerCase()
                        .trim();

        for (FoodDonation offer :
                availableOffers) {

            boolean matchesSearch =
                    offer.getFoodName()
                            .toLowerCase()
                            .contains(
                                    searchText
                            );

            boolean matchesFilter;

            if (currentFilter == 0) {

                matchesFilter = true;

            } else if (currentFilter == 1) {

                matchesFilter =
                        offer.getQuantity() <= 5;

            } else {

                matchesFilter =
                        offer.getQuantity() > 5;
            }

            if (matchesSearch
                    && matchesFilter) {

                filteredOffers.add(
                        offer
                );
            }
        }

        // ------------------------------------------------
        // Calculate Distances
        // ------------------------------------------------

        Map<Integer, Double> distanceMap =
                calculateDistances(
                        filteredOffers
                );

        // ------------------------------------------------
        // Update Adapter
        // ------------------------------------------------

        offerAdapter.updateOffers(
                filteredOffers,
                distanceMap
        );

        updateEmptyState();
    }

    // ====================================================
    // CALCULATE DISTANCES
    // ====================================================

    private Map<Integer, Double>
    calculateDistances(
            List<FoodDonation> offers) {

        Map<Integer, Double> distances =
                new HashMap<>();

        if (charityLatitude == 0.0
                && charityLongitude == 0.0) {

            return distances;
        }

        if (offers == null) {

            return distances;
        }

        for (FoodDonation offer :
                offers) {

            double[] organizationLocation =
                    foodDonationRepository
                            .getOrganizationLocation(
                                    offer.getFoodOrganizationId()
                            );

            if (organizationLocation == null) {

                continue;
            }

            double organizationLatitude =
                    organizationLocation[0];

            double organizationLongitude =
                    organizationLocation[1];

            if (organizationLatitude == 0.0
                    && organizationLongitude == 0.0) {

                continue;
            }

            double distance =
                    DistanceUtils
                            .calculateDistanceInKm(
                                    charityLatitude,
                                    charityLongitude,
                                    organizationLatitude,
                                    organizationLongitude
                            );

            distances.put(
                    offer.getId(),
                    distance
            );
        }

        return distances;
    }

    // ====================================================
    // EMPTY STATE
    // ====================================================

    private void updateEmptyState() {

        boolean empty =
                filteredOffers == null
                        || filteredOffers.isEmpty();

        if (empty) {

            recyclerViewOffers.setVisibility(
                    View.GONE
            );

            layoutEmptyOffers.setVisibility(
                    View.VISIBLE
            );

        } else {

            recyclerViewOffers.setVisibility(
                    View.VISIBLE
            );

            layoutEmptyOffers.setVisibility(
                    View.GONE
            );
        }
    }

    // ====================================================
    // OPEN OFFER DETAILS
    // ====================================================

    private void openOfferDetails(
            FoodDonation offer) {

        if (!(requireActivity()
                instanceof CharityMainActivity)) {

            return;
        }

        CharityMainActivity mainActivity =
                (CharityMainActivity)
                        requireActivity();

        Map<Integer, Double> distanceMap =
                calculateDistances(
                        filteredOffers
                );

        double distance =
                -1;

        if (distanceMap.containsKey(
                offer.getId()
        )) {

            distance =
                    distanceMap.get(
                            offer.getId()
                    );
        }

        mainActivity.openOfferDetails(
                offer.getId(),
                distance
        );
    }

    // ====================================================
    // DATABASE LIFECYCLE
    // ====================================================

    @Override
    public void onDestroyView() {

        if (foodDonationRepository != null) {

            foodDonationRepository.close();
        }

        if (userRepository != null) {

            userRepository.close();
        }

        super.onDestroyView();
    }
}