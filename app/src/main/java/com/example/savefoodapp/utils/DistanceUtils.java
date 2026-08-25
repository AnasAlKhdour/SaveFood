package com.example.savefoodapp.utils;

import android.location.Location;

public final class DistanceUtils {

    private DistanceUtils() {
        // Prevent instantiation
    }

    /**
     * Calculate distance between two geographic coordinates.
     *
     * @return distance in kilometers
     */
    public static double calculateDistanceInKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {

        float[] results =
                new float[1];

        Location.distanceBetween(
                latitude1,
                longitude1,
                latitude2,
                longitude2,
                results
        );

        return results[0] / 1000.0;
    }
}