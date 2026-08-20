package com.example.savefoodapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationFailed();
    }

    public static void getLocation(
            Activity activity,
            LocationCallback callback
    ) {

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            callback.onLocationFailed();
            return;
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(activity);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            callback.onLocationReceived(location);
                        } else {
                            callback.onLocationFailed();
                        }
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onLocationFailed();
                    }
                });
    }
    public static double getLatitude(Location location) {
        return location.getLatitude();
    }

    public static double getLongitude(Location location) {
        return location.getLongitude();
    }
}
