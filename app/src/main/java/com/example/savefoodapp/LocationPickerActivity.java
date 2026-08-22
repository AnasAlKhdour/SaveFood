package com.example.savefoodapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.example.savefoodapp.utils.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickerActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Button btnConfirmLocation;

    private Marker selectedMarker;
    private LatLng selectedLocation;

    private static final String MAP_VIEW_BUNDLE_KEY =
            "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_location_picker
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        mapView =
                findViewById(R.id.mapView);

        btnConfirmLocation =
                findViewById(R.id.btnConfirmLocation);

        Bundle mapViewBundle = null;

        if (savedInstanceState != null) {

            mapViewBundle =
                    savedInstanceState.getBundle(
                            MAP_VIEW_BUNDLE_KEY
                    );
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

        btnConfirmLocation.setOnClickListener(
                v -> confirmLocation()
        );
    }

    @Override
    public void onMapReady(
            @NonNull GoogleMap map
    ) {

        googleMap = map;

        // Map UI settings
        googleMap.getUiSettings()
                .setZoomControlsEnabled(true);

        googleMap.getUiSettings()
                .setCompassEnabled(true);

        googleMap.getUiSettings()
                .setMyLocationButtonEnabled(true);

        /*
         * User selects the organization location
         * manually by tapping anywhere on the map.
         */
        googleMap.setOnMapClickListener(
                latLng -> selectLocation(latLng)
        );

        checkLocationPermission();
    }

    /**
     * Check whether location permission
     * was already granted.
     *
     * Permission is requested from RegisterActivity,
     * not from this activity.
     */
    private void checkLocationPermission() {

        boolean fineGranted =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {

            /*
             * Permission should already have been requested
             * in RegisterActivity.
             *
             * We do NOT request it again here.
             */
            moveToDefaultLocation();

            Toast.makeText(
                    this,
                    R.string.manual_location_selection,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        enableMyLocation();

        getCurrentLocation();
    }

    /**
     * Enable the blue current-location indicator.
     */
    private void enableMyLocation() {

        if (googleMap == null) {
            return;
        }

        boolean fineGranted =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
    }

    /**
     * Get the user's current location only
     * to position the camera.
     *
     * IMPORTANT:
     * Current location is NOT automatically selected
     * as the organization location.
     */
    private void getCurrentLocation() {

        LocationHelper.getLocation(
                this,
                new LocationHelper.LocationCallback() {

                    @Override
                    public void onLocationReceived(
                            Location location
                    ) {

                        LatLng currentLocation =
                                new LatLng(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );

                        /*
                         * Move the camera to the user's
                         * current location.
                         *
                         * Do NOT assign this location
                         * to selectedLocation.
                         */
                        googleMap.animateCamera(
                                CameraUpdateFactory
                                        .newLatLngZoom(
                                                currentLocation,
                                                16f
                                        )
                        );
                    }

                    @Override
                    public void onLocationFailed() {

                        moveToDefaultLocation();

                        Toast.makeText(
                                LocationPickerActivity.this,
                                R.string.location_not_available,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    /**
     * Select organization location manually.
     */
    private void selectLocation(
            LatLng latLng
    ) {

        selectedLocation = latLng;

        // Remove previous selected marker
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        // Add new selected marker
        selectedMarker =
                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(
                                        getString(
                                                R.string.selected_location
                                        )
                                )
                );

        // Move camera to selected location
        googleMap.animateCamera(
                CameraUpdateFactory
                        .newLatLngZoom(
                                latLng,
                                16f
                        )
        );

        Toast.makeText(
                this,
                R.string.location_selected,
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * Default location if current location
     * cannot be obtained.
     */
    private void moveToDefaultLocation() {

        if (googleMap == null) {
            return;
        }

        // Amman, Jordan
        LatLng defaultLocation =
                new LatLng(
                        31.9539,
                        35.9106
                );

        googleMap.moveCamera(
                CameraUpdateFactory
                        .newLatLngZoom(
                                defaultLocation,
                                10f
                        )
        );
    }

    /**
     * Return selected coordinates to RegisterActivity.
     */
    private void confirmLocation() {

        if (selectedLocation == null) {

            Toast.makeText(
                    this,
                    R.string.select_location_first,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent resultIntent =
                new Intent();

        resultIntent.putExtra(
                "latitude",
                selectedLocation.latitude
        );

        resultIntent.putExtra(
                "longitude",
                selectedLocation.longitude
        );

        setResult(
                RESULT_OK,
                resultIntent
        );

        finish();
    }

    // ------------------------------------------------
    // MapView Lifecycle
    // ------------------------------------------------

    @Override
    protected void onStart() {

        super.onStart();

        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        if (mapView != null) {
            mapView.onStop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (mapView != null) {
            mapView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {

        super.onLowMemory();

        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle outState
    ) {

        Bundle mapViewBundle =
                outState.getBundle(
                        MAP_VIEW_BUNDLE_KEY
                );

        if (mapViewBundle == null) {

            mapViewBundle =
                    new Bundle();

            outState.putBundle(
                    MAP_VIEW_BUNDLE_KEY,
                    mapViewBundle
            );
        }

        mapView.onSaveInstanceState(
                mapViewBundle
        );

        super.onSaveInstanceState(outState);
    }
}