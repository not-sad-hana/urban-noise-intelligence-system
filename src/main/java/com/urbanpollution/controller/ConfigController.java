package com.urbanpollution.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public configuration endpoints for the frontend.
 * These endpoints don't require authentication.
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    /**
     * Get frontend configuration including Google Maps API key
     */
    @GetMapping("/maps")
    public ResponseEntity<?> getMapsConfig() {
        // Read from environment variable
        String apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("apiKey", apiKey != null ? apiKey : "");
        config.put("center", Map.of(
                "lat", 13.030,
                "lng", 80.182
        ));
        config.put("zoom", 14);
        config.put("bounds", Map.of(
                "minLat", 13.010,
                "maxLat", 13.050,
                "minLng", 80.165,
                "maxLng", 80.200
        ));
        
        return ResponseEntity.ok(config);
    }

    /**
     * Get application health/status (public endpoint)
     */
    @GetMapping("/health")
    public ResponseEntity<?> getAppHealth() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "healthy");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("area", "Ramapuram, Chennai");
        status.put("bounds", Map.of(
                "minLat", 13.010,
                "maxLat", 13.050,
                "minLng", 80.165,
                "maxLng", 80.200
        ));
        return ResponseEntity.ok(status);
    }
}
