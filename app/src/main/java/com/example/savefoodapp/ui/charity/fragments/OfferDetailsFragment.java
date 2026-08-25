package com.example.savefoodapp.ui.charity.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.image.ImageUtils;
import com.example.savefoodapp.data.models.FoodDonation;
import com.example.savefoodapp.data.repository.FoodDonationRepository;
import com.example.savefoodapp.ui.charity.bottomsheet.RequestFoodBottomSheet;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class OfferDetailsFragment
        extends Fragment {

    private static final String ARG_OFFER_ID =
            "OFFER_ID";

    private static final String ARG_DISTANCE =
            "DISTANCE";

    private int donationId;

    private double distance;

    private FoodDonation donation;

    private FoodDonationRepository foodDonationRepository;

    public OfferDetailsFragment() {
        // Required empty constructor
    }

    public static OfferDetailsFragment newInstance(
            int offerId,
            double distance
    ) {

        OfferDetailsFragment fragment =
                new OfferDetailsFragment();

        Bundle args =
                new Bundle();

        args.putInt(
                ARG_OFFER_ID,
                offerId
        );

        args.putDouble(
                ARG_DISTANCE,
                distance
        );

        fragment.setArguments(
                args
        );

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_offer_details,
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
        // Get Arguments
        // ------------------------------------------------

        if (getArguments() != null) {

            donationId =
                    getArguments().getInt(
                            ARG_OFFER_ID,
                            -1
                    );

            distance =
                    getArguments().getDouble(
                            ARG_DISTANCE,
                            -1
                    );
        }

        if (donationId <= 0) {

            Toast.makeText(
                    requireContext(),
                    "Offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

            return;
        }

        // ------------------------------------------------
        // Repository
        // ------------------------------------------------

        foodDonationRepository =
                new FoodDonationRepository(
                        requireContext()
                );

        foodDonationRepository.open();

        // ------------------------------------------------
        // Load Offer
        // ------------------------------------------------

        loadOffer(
                view
        );
    }

    // ====================================================
    // LOAD OFFER
    // ====================================================

    private void loadOffer(
            View view
    ) {

        donation =
                foodDonationRepository
                        .getFoodDonationById(
                                donationId
                        );

        if (donation == null) {

            Toast.makeText(
                    requireContext(),
                    "Offer not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ------------------------------------------------
        // UI References
        // ------------------------------------------------

        ImageView imgOffer =
                view.findViewById(
                        R.id.imgOfferDetails
                );

        TextView tvFoodName =
                view.findViewById(
                        R.id.tvFoodName
                );

        TextView tvQuantity =
                view.findViewById(
                        R.id.tvQuantity
                );

        TextView tvExpiryDate =
                view.findViewById(
                        R.id.tvExpiryDate
                );

        TextView tvDistance =
                view.findViewById(
                        R.id.tvDistance
                );

        TextView tvDescription =
                view.findViewById(
                        R.id.tvDescription
                );

        TextView tvStatus =
                view.findViewById(
                        R.id.tvStatus
                );

        MaterialButton btnRequestFood =
                view.findViewById(
                        R.id.btnRequestFood
                );

        // ------------------------------------------------
        // Display Offer
        // ------------------------------------------------

        tvFoodName.setText(
                donation.getFoodName()
        );

        tvQuantity.setText(
                "Available: "
                        + donation.getQuantity()
                        + " units"
        );

        tvExpiryDate.setText(
                "Expires: "
                        + donation.getExpiryDate()
        );

        tvDescription.setText(
                donation.getDescription()
        );

        tvStatus.setText(
                donation.getStatus()
        );

        // ------------------------------------------------
        // Display Distance
        // ------------------------------------------------

        if (distance >= 0) {

            tvDistance.setText(
                    String.format(
                            Locale.US,
                            "Distance: %.2f km",
                            distance
                    )
            );

        } else {

            tvDistance.setText(
                    "Distance: Not available"
            );
        }

        // ------------------------------------------------
        // Load Image
        // ------------------------------------------------

        String imagePath =
                donation.getImagePath();

        if (imagePath != null
                && !imagePath.isEmpty()) {

            Bitmap bitmap =
                    ImageUtils.decodeSampled(
                            imagePath,
                            1200,
                            700
                    );

            if (bitmap != null) {

                imgOffer.setImageBitmap(
                        bitmap
                );

            } else {

                imgOffer.setImageResource(
                        android.R.drawable.ic_menu_gallery
                );
            }

        } else {

            imgOffer.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        // ------------------------------------------------
        // Request Button
        // ------------------------------------------------

        boolean canRequest =
                "AVAILABLE".equalsIgnoreCase(
                        donation.getStatus()
                )
                        && donation.getQuantity() > 0;

        btnRequestFood.setEnabled(
                canRequest
        );

        btnRequestFood.setOnClickListener(
                v -> openRequestFoodBottomSheet()
        );
    }

    // ====================================================
    // OPEN REQUEST FOOD BOTTOM SHEET
    // ====================================================

    private void openRequestFoodBottomSheet() {

        if (donation == null) {

            return;
        }

        if (!"AVAILABLE".equalsIgnoreCase(
                donation.getStatus()
        )
                || donation.getQuantity() <= 0) {

            Toast.makeText(
                    requireContext(),
                    "This offer is no longer available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        RequestFoodBottomSheet bottomSheet =
                RequestFoodBottomSheet.newInstance(
                        donation.getId(),
                        donation.getFoodName(),
                        donation.getQuantity()
                );

        bottomSheet.show(
                getParentFragmentManager(),
                "RequestFoodBottomSheet"
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

        super.onDestroyView();
    }
}