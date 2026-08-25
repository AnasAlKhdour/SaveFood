package com.example.savefoodapp.ui.food;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.savefoodapp.R;
import com.example.savefoodapp.core.session.SessionManager;
import com.example.savefoodapp.ui.auth.LoginActivity;
import com.example.savefoodapp.ui.food.fragments.AddOfferFragment;
import com.example.savefoodapp.ui.food.fragments.FoodHomeFragment;
import com.example.savefoodapp.ui.food.fragments.MyOffersFragment;
import com.example.savefoodapp.ui.food.fragments.ProfileFragment;
import com.example.savefoodapp.ui.food.fragments.RequestsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FoodMainActivity
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
                R.layout.activity_food_main
        );

        // ------------------------------------------------
        // Initialize Views
        // ------------------------------------------------

        toolbar =
                findViewById(
                        R.id.toolbar
                );

        bottomNavigationView =
                findViewById(
                        R.id.bottomNavigationView
                );

        // ------------------------------------------------
        // Session
        // ------------------------------------------------

        sessionManager =
                new SessionManager(this);

        // ------------------------------------------------
        // Toolbar
        // ------------------------------------------------

        setSupportActionBar(toolbar);

        toolbar.setOverflowIcon(
                androidx.core.content.ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_more_vertical
                )
        );

        // ------------------------------------------------
        // Default Toolbar Title
        // ------------------------------------------------

        updateToolbarTitle(
                "Home"
        );

        // ------------------------------------------------
        // Bottom Navigation
        // ------------------------------------------------

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
                                        == R.id.food_nav_home) {

                                    clearFragmentBackStack();

                                    openFragment(
                                            new FoodHomeFragment()
                                    );

                                    updateToolbarTitle(
                                            "Home"
                                    );

                                    return true;
                                }

                                // --------------------------------
                                // My Offers
                                // --------------------------------

                                if (itemId
                                        == R.id.food_nav_offers) {

                                    clearFragmentBackStack();

                                    openFragment(
                                            new MyOffersFragment()
                                    );

                                    updateToolbarTitle(
                                            "My Offers"
                                    );

                                    return true;
                                }

                                // --------------------------------
                                // Add Offer
                                // --------------------------------

                                if (itemId
                                        == R.id.food_nav_add_offer) {

                                    clearFragmentBackStack();

                                    openFragment(
                                            new AddOfferFragment()
                                    );

                                    updateToolbarTitle(
                                            "Add Offer"
                                    );

                                    return true;
                                }

                                // --------------------------------
                                // Requests
                                // --------------------------------

                                if (itemId
                                        == R.id.food_nav_requests) {

                                    clearFragmentBackStack();

                                    openFragment(
                                            new RequestsFragment()
                                    );

                                    updateToolbarTitle(
                                            "Requests"
                                    );

                                    return true;
                                }

                                return false;
                            }
                        }
                );

        // ------------------------------------------------
        // Default Fragment
        // ------------------------------------------------

        if (savedInstanceState == null) {

            bottomNavigationView
                    .setSelectedItemId(
                            R.id.food_nav_home
                    );
        }

        // ------------------------------------------------
        // Back Button
        // ------------------------------------------------

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

                                // --------------------------------
                                // 1. Fragment Back Stack
                                // --------------------------------

                                if (getSupportFragmentManager()
                                        .getBackStackEntryCount() > 0) {

                                    getSupportFragmentManager()
                                            .popBackStack();

                                    return;
                                }

                                // --------------------------------
                                // 2. Non-Home Bottom Tab
                                // --------------------------------

                                if (bottomNavigationView
                                        .getSelectedItemId()
                                        != R.id.food_nav_home) {

                                    bottomNavigationView
                                            .setSelectedItemId(
                                                    R.id.food_nav_home
                                            );

                                    return;
                                }

                                // --------------------------------
                                // 3. Home -> Exit App
                                // --------------------------------

                                finish();
                            }
                        }
                );
    }

    // ====================================================
    // CLEAR FRAGMENT BACK STACK
    // ====================================================

    private void clearFragmentBackStack() {

        getSupportFragmentManager()
                .popBackStack(
                        null,
                        androidx.fragment.app.FragmentManager
                                .POP_BACK_STACK_INCLUSIVE
                );
    }

    // ====================================================
    // OVERFLOW MENU
    // ====================================================

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {

        getMenuInflater().inflate(
                R.menu.food_overflow_menu,
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
    // OPEN FRAGMENT
    // ====================================================

    private void openFragment(
            Fragment fragment
    ) {

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .commit();
    }

    // ====================================================
    // TOOLBAR TITLE
    // ====================================================

    public void updateToolbarTitle(
            String title
    ) {

        if (getSupportActionBar()
                != null) {

            getSupportActionBar()
                    .setTitle(title);
        }
    }

    // ====================================================
    // PROFILE
    // ====================================================

    private void openProfile() {

        // --------------------------------
        // Clear Previous Back Stack
        // --------------------------------

        clearFragmentBackStack();

        // --------------------------------
        // Open Profile
        // --------------------------------

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

        updateToolbarTitle(
                "Profile"
        );
    }

    // ====================================================
    // OPEN MY OFFERS
    // ====================================================

    public void openMyOffers() {

        clearFragmentBackStack();

        bottomNavigationView.setSelectedItemId(
                R.id.food_nav_offers
        );
    }

    // ====================================================
    // LOGOUT
    // ====================================================

    private void logout() {

        sessionManager.logout();

        Intent intent =
                new Intent(
                        FoodMainActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}