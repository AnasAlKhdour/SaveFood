package com.example.savefoodapp.ui.charity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.ui.auth.LoginActivity;
import com.example.savefoodapp.ui.charity.fragments.CharityHomeFragment;
import com.example.savefoodapp.ui.charity.fragments.OfferDetailsFragment;
import com.example.savefoodapp.ui.charity.fragments.OffersFragment;
import com.example.savefoodapp.ui.charity.fragments.ProfileFragment;
import com.example.savefoodapp.ui.charity.fragments.RequestsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CharityMainActivity
        extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_charity_main
        );

        // ====================================================
        // INITIALIZE VIEWS
        // ====================================================

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        bottomNavigationView =
                findViewById(
                        R.id.bottomNavigationView
                );

        // ====================================================
        // SESSION
        // ====================================================

        sessionManager =
                new SessionManager(this);

        // ====================================================
        // TOOLBAR
        // ====================================================

        setSupportActionBar(toolbar);

        toolbar.setOverflowIcon(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_more_vertical
                )
        );

        updateToolbarTitle(
                "Home"
        );

        // ====================================================
        // BOTTOM NAVIGATION
        // ====================================================

        bottomNavigationView
                .setOnItemSelectedListener(
                        new BottomNavigationView
                                .OnItemSelectedListener() {

                            @Override
                            public boolean
                            onNavigationItemSelected(
                                    @NonNull MenuItem item
                            ) {

                                int itemId =
                                        item.getItemId();

                                // --------------------------------
                                // Home
                                // --------------------------------

                                if (itemId
                                        == R.id.nav_home) {

                                    showMainFragment(
                                            new CharityHomeFragment(),
                                            "Home"
                                    );

                                    return true;
                                }

                                // --------------------------------
                                // Offers
                                // --------------------------------

                                if (itemId
                                        == R.id.nav_offers) {

                                    showMainFragment(
                                            new OffersFragment(),
                                            "Food Offers"
                                    );

                                    return true;
                                }

                                // --------------------------------
                                // Requests
                                // --------------------------------

                                if (itemId
                                        == R.id.nav_requests) {

                                    showMainFragment(
                                            new RequestsFragment(),
                                            "My Requests"
                                    );

                                    return true;
                                }

                                return false;
                            }
                        }
                );

        // ====================================================
        // DEFAULT FRAGMENT
        // ====================================================

        if (savedInstanceState == null) {

            bottomNavigationView
                    .setSelectedItemId(
                            R.id.nav_home
                    );
        }

        // ====================================================
        // BACK HANDLING
        // ====================================================

        setupBackButton();
    }

    // ====================================================
    // BACK BUTTON
    // ====================================================

    private void setupBackButton() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                FragmentManager fragmentManager =
                                        getSupportFragmentManager();

                                // --------------------------------
                                // Fragment Back Stack
                                // --------------------------------

                                if (fragmentManager
                                        .getBackStackEntryCount() > 0) {

                                    fragmentManager
                                            .popBackStackImmediate();

                                    updateAfterBackStackPop();

                                    return;
                                }

                                // --------------------------------
                                // Non-Home Bottom Tab
                                // --------------------------------

                                if (bottomNavigationView
                                        .getSelectedItemId()
                                        != R.id.nav_home) {

                                    bottomNavigationView
                                            .setSelectedItemId(
                                                    R.id.nav_home
                                            );

                                    return;
                                }

                                // --------------------------------
                                // Home -> Exit App
                                // --------------------------------

                                finish();
                            }
                        }
                );
    }

    // ====================================================
    // UPDATE UI AFTER BACK
    // ====================================================

    private void updateAfterBackStackPop() {

        Fragment currentFragment =
                getSupportFragmentManager()
                        .findFragmentById(
                                R.id.fragmentContainer
                        );

        // ------------------------------------------------
        // Profile
        // ------------------------------------------------

        if (currentFragment
                instanceof ProfileFragment) {

            bottomNavigationView.setVisibility(
                    BottomNavigationView.GONE
            );

            updateToolbarTitle(
                    "Profile"
            );

            return;
        }

        // ------------------------------------------------
        // Offer Details
        // ------------------------------------------------

        if (currentFragment
                instanceof OfferDetailsFragment) {

            bottomNavigationView.setVisibility(
                    BottomNavigationView.GONE
            );

            updateToolbarTitle(
                    "Offer Details"
            );

            return;
        }

        // ------------------------------------------------
        // Main Charity Fragments
        // ------------------------------------------------

        bottomNavigationView.setVisibility(
                BottomNavigationView.VISIBLE
        );

        if (currentFragment
                instanceof CharityHomeFragment) {

            bottomNavigationView
                    .setSelectedItemId(
                            R.id.nav_home
                    );

            updateToolbarTitle(
                    "Home"
            );

        } else if (currentFragment
                instanceof OffersFragment) {

            bottomNavigationView
                    .setSelectedItemId(
                            R.id.nav_offers
                    );

            updateToolbarTitle(
                    "Food Offers"
            );

        } else if (currentFragment
                instanceof RequestsFragment) {

            bottomNavigationView
                    .setSelectedItemId(
                            R.id.nav_requests
                    );

            updateToolbarTitle(
                    "My Requests"
            );
        }
    }

    // ====================================================
    // SHOW MAIN FRAGMENT
    // ====================================================

    private void showMainFragment(
            Fragment fragment,
            String title
    ) {

        // ------------------------------------------------
        // Clear Nested Fragment History
        // ------------------------------------------------

        getSupportFragmentManager()
                .popBackStack(
                        null,
                        FragmentManager
                                .POP_BACK_STACK_INCLUSIVE
                );

        // ------------------------------------------------
        // Open Main Fragment
        // ------------------------------------------------

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .commit();

        // ------------------------------------------------
        // Toolbar
        // ------------------------------------------------

        updateToolbarTitle(
                title
        );

        // ------------------------------------------------
        // Bottom Navigation
        // ------------------------------------------------

        bottomNavigationView.setVisibility(
                BottomNavigationView.VISIBLE
        );
    }

    // ====================================================
    // OPEN OFFER DETAILS
    // ====================================================

    public void openOfferDetails(
            int offerId,
            double distance
    ) {

        OfferDetailsFragment fragment =
                OfferDetailsFragment.newInstance(
                        offerId,
                        distance
                );

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .addToBackStack(
                        "offer_details"
                )
                .commit();

        updateToolbarTitle(
                "Offer Details"
        );

        bottomNavigationView.setVisibility(
                BottomNavigationView.GONE
        );
    }

    // ====================================================
    // UPDATE TOOLBAR TITLE
    // ====================================================

    public void updateToolbarTitle(
            String title
    ) {

        if (getSupportActionBar()
                != null) {

            getSupportActionBar()
                    .setTitle(
                            title
                    );
        }
    }

    // ====================================================
    // LOGOUT
    // ====================================================

    private void logout() {

        sessionManager.logout();

        Intent intent =
                new Intent(
                        CharityMainActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // ====================================================
    // OPEN PROFILE
    // ====================================================

    private void openProfile() {

        // ------------------------------------------------
        // Clear Previous Fragment History
        // ------------------------------------------------

        getSupportFragmentManager()
                .popBackStack(
                        null,
                        FragmentManager
                                .POP_BACK_STACK_INCLUSIVE
                );

        // ------------------------------------------------
        // Open Profile
        // ------------------------------------------------

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragmentContainer,
                        new ProfileFragment()
                )
                .addToBackStack(
                        "profile"
                )
                .commit();

        // ------------------------------------------------
        // Toolbar
        // ------------------------------------------------

        updateToolbarTitle(
                "Profile"
        );

        // ------------------------------------------------
        // Bottom Navigation
        // ------------------------------------------------

        bottomNavigationView.setVisibility(
                BottomNavigationView.GONE
        );
    }

    // ====================================================
    // OPTIONS MENU
    // ====================================================

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {

        getMenuInflater().inflate(
                R.menu.charity_overflow_menu,
                menu
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            @NonNull MenuItem item
    ) {

        // ------------------------------------------------
        // Profile
        // ------------------------------------------------

        if (item.getItemId()
                == R.id.menu_profile) {

            openProfile();

            return true;
        }

        // ------------------------------------------------
        // Logout
        // ------------------------------------------------

        if (item.getItemId()
                == R.id.menu_logout) {

            logout();

            return true;
        }

        return super.onOptionsItemSelected(
                item
        );
    }

    // ====================================================
    // OPEN MY REQUESTS
    // ====================================================

    public void openMyRequests() {

        bottomNavigationView.setSelectedItemId(
                R.id.nav_requests
        );
    }

    // ====================================================
    // SET PROFILE TOOLBAR TITLE
    // ====================================================

    public void setProfileToolbarTitle(
            String title
    ) {

        updateToolbarTitle(
                title
        );
    }
}