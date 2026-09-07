package com.urbanpollution.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to enforce the Ramapuram, Chennai Geo-Fencing Area Lock.
 * Bounding box: lat 13.010–13.050, lng 80.165–80.200.
 * All map views, markers, and data inserts must be restricted/clamped to this box.
 */
@Service
public class GeoFencingService {

    @Value("${ramapuram.bounds.lat.min:13.010000}")
    private double minLat;

    @Value("${ramapuram.bounds.lat.max:13.050000}")
    private double maxLat;

    @Value("${ramapuram.bounds.lng.min:80.165000}")
    private double minLng;

    @Value("${ramapuram.bounds.lng.max:80.200000}")
    private double maxLng;

    /**
     * Checks if coordinates fall within the Ramapuram bounding box.
     */
    public boolean isWithinBounds(double lat, double lng) {
        return lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
    }

    /**
     * Validates coordinates and throws IllegalArgumentException if out of bounds.
     */
    public void validateWithinBounds(double lat, double lng) {
        if (!isWithinBounds(lat, lng)) {
            throw new IllegalArgumentException(String.format(
                "Area Lock Violation: Location (%.6f, %.6f) is outside Ramapuram bounding box [Lat: %.3f-%.3f, Lng: %.3f-%.3f]",
                lat, lng, minLat, maxLat, minLng, maxLng
            ));
        }
    }

    /**
     * Clamps latitude to the allowed bounding box range.
     */
    public double clampLat(double lat) {
        return Math.max(minLat, Math.min(maxLat, lat));
    }

    /**
     * Clamps longitude to the allowed bounding box range.
     */
    public double clampLng(double lng) {
        return Math.max(minLng, Math.min(maxLng, lng));
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMinLng() {
        return minLng;
    }

    public double getMaxLng() {
        return maxLng;
    }
}
