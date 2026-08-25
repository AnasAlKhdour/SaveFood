package com.example.savefoodapp.ui.charity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savefoodapp.R;
import com.example.savefoodapp.data.models.DonationRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter
        extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<DonationRequest> requests;

    public RequestAdapter(
            List<DonationRequest> requests
    ) {
        this.requests =
                requests != null
                        ? requests
                        : new ArrayList<>();
    }

    public void updateRequests(
            List<DonationRequest> newRequests
    ) {

        this.requests =
                newRequests != null
                        ? newRequests
                        : new ArrayList<>();

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_request,
                        parent,
                        false
                );

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder,
            int position
    ) {

        DonationRequest request =
                requests.get(position);

        // ------------------------------------------------
        // Default Food Name
        // ------------------------------------------------

        holder.tvFoodName.setText(
                "Food Donation"
        );

        // ------------------------------------------------
        // Quantity
        // ------------------------------------------------

        holder.tvQuantity.setText(
                "Requested: "
                        + request.getQuantityRequested()
                        + " units"
        );

        // ------------------------------------------------
        // Status
        // ------------------------------------------------

        String status =
                request.getStatus();

        if (status == null
                || status.isEmpty()) {

            status = "UNKNOWN";
        }

        holder.tvStatus.setText(
                status
        );

        // ------------------------------------------------
        // Status Styling
        // ------------------------------------------------

        int statusColor;
        int statusBackground;

        switch (
                status.toUpperCase()
        ) {

            case "ACCEPTED":

                statusColor =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_success
                        );

                statusBackground =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_success_light
                        );

                break;

            case "REJECTED":

                statusColor =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_error
                        );

                statusBackground =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_error_light
                        );

                break;

            case "PENDING":
            default:

                statusColor =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_warning
                        );

                statusBackground =
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.savefood_warning_light
                        );

                break;
        }

        holder.tvStatus.setTextColor(
                statusColor
        );

        holder.tvStatus.setBackgroundColor(
                statusBackground
        );
    }

    @Override
    public int getItemCount() {

        return requests.size();
    }

    static class RequestViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvFoodName;
        TextView tvQuantity;
        TextView tvStatus;

        RequestViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvFoodName =
                    itemView.findViewById(
                            R.id.tvFoodName
                    );

            tvQuantity =
                    itemView.findViewById(
                            R.id.tvQuantity
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvStatus
                    );
        }
    }
}