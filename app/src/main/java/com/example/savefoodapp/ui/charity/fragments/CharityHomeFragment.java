package com.example.savefoodapp.ui.charity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;

public class CharityHomeFragment extends Fragment {

    private SessionManager sessionManager;

    private TextView tvGreeting;
    private TextView tvOrganizationName;

    public CharityHomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_charity_home,
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
        // Initialize
        // ------------------------------------------------

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        tvGreeting =
                view.findViewById(
                        R.id.tvGreeting
                );

        tvOrganizationName =
                view.findViewById(
                        R.id.tvOrganizationName
                );

        // ------------------------------------------------
        // Load User Information
        // ------------------------------------------------

        String userName =
                sessionManager.getUserName();

        if (userName != null
                && !userName.isEmpty()) {

            tvGreeting.setText(
                    "Welcome back 👋"
            );

            tvOrganizationName.setText(
                    userName
            );

        } else {

            tvGreeting.setText(
                    "Welcome back 👋"
            );

            tvOrganizationName.setText(
                    "Charity Organization"
            );
        }
    }
}