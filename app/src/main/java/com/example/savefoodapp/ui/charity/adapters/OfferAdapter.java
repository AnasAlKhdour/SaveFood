package com.example.savefoodapp.ui.charity.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.image.ImageUtils;
import com.example.savefoodapp.data.models.FoodDonation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OfferAdapter
        extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    public interface OnOfferClickListener {

        void onOfferClick(
                FoodDonation offer
        );
    }

    private List<FoodDonation> offers;

    private Map<Integer, Double> distanceByOfferId;

    private final OnOfferClickListener listener;

    public OfferAdapter(
            List<FoodDonation> offers,
            Map<Integer, Double> distanceByOfferId,
            OnOfferClickListener listener
    ) {

        this.offers =
                offers != null
                        ? offers
                        : new ArrayList<>();

        this.distanceByOfferId =
                distanceByOfferId != null
                        ? distanceByOfferId
                        : new HashMap<>();

        this.listener = listener;
    }

    /**
     * Update offers and their calculated distances.
     */
    public void updateOffers(
            List<FoodDonation> newOffers,
            Map<Integer, Double> newDistances
    ) {

        this.offers =
                newOffers != null
                        ? newOffers
                        : new ArrayList<>();

        this.distanceByOfferId =
                newDistances != null
                        ? newDistances
                        : new HashMap<>();

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_offer,
                        parent,
                        false
                );

        return new OfferViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull OfferViewHolder holder,
            int position
    ) {

        FoodDonation offer =
                offers.get(position);

        // ------------------------------------------------
        // Food Name
        // ------------------------------------------------

        holder.tvFoodName.setText(
                offer.getFoodName()
        );

        // ------------------------------------------------
        // Quantity
        // ------------------------------------------------

        holder.tvQuantity.setText(
                "Available: "
                        + offer.getQuantity()
                        + " units"
        );

        // ------------------------------------------------
        // Expiry
        // ------------------------------------------------

        holder.tvExpiryDate.setText(
                "Expires: "
                        + offer.getExpiryDate()
        );

        // ------------------------------------------------
        // Status
        // ------------------------------------------------

        holder.tvStatus.setText(
                offer.getStatus()
        );

        // ------------------------------------------------
        // Distance
        // ------------------------------------------------

        Double distance =
                distanceByOfferId.get(
                        offer.getId()
                );

        if (distance != null
                && distance >= 0) {

            holder.tvDistance.setText(
                    String.format(
                            Locale.US,
                            "%.2f km",
                            distance
                    )
            );

        } else {

            holder.tvDistance.setText(
                    "Distance unavailable"
            );
        }

        // ------------------------------------------------
        // Image
        // ------------------------------------------------

        String imagePath =
                offer.getImagePath();

        if (imagePath != null
                && !imagePath.isEmpty()) {

            Bitmap bitmap =
                    ImageUtils.decodeSampled(
                            imagePath,
                            1000,
                            500
                    );

            if (bitmap != null) {

                holder.imgOffer.setImageBitmap(
                        bitmap
                );

            } else {

                holder.imgOffer.setImageResource(
                        android.R.drawable.ic_menu_gallery
                );
            }

        } else {

            holder.imgOffer.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        // ------------------------------------------------
        // Click
        // ------------------------------------------------

        holder.itemView.setOnClickListener(
                view -> {

                    if (listener != null) {

                        listener.onOfferClick(
                                offer
                        );
                    }
                }
        );
    }

    @Override
    public int getItemCount() {

        return offers.size();
    }

    // ====================================================
    // VIEW HOLDER
    // ====================================================

    static class OfferViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgOffer;

        TextView tvFoodName;
        TextView tvQuantity;
        TextView tvExpiryDate;
        TextView tvDistance;
        TextView tvStatus;

        OfferViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            imgOffer =
                    itemView.findViewById(
                            R.id.imgOffer
                    );

            tvFoodName =
                    itemView.findViewById(
                            R.id.tvFoodName
                    );

            tvQuantity =
                    itemView.findViewById(
                            R.id.tvQuantity
                    );

            tvExpiryDate =
                    itemView.findViewById(
                            R.id.tvExpiryDate
                    );

            tvDistance =
                    itemView.findViewById(
                            R.id.tvDistance
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvStatus
                    );
        }
    }
}