package com.urbanpollution.controller;

import com.urbanpollution.model.Location;
import com.urbanpollution.model.PollutionType;
import com.urbanpollution.model.Reading;
import com.urbanpollution.repository.LocationRepository;
import com.urbanpollution.repository.PollutionTypeRepository;
import com.urbanpollution.repository.ReadingRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/pollution")
@CrossOrigin(origins = "*")
public class PollutionDataController {

    private final ReadingRepository readingRepository;
    private final LocationRepository locationRepository;
    private final PollutionTypeRepository pollutionTypeRepository;

    public PollutionDataController(ReadingRepository readingRepository,
                                   LocationRepository locationRepository,
                                   PollutionTypeRepository pollutionTypeRepository) {
        this.readingRepository = readingRepository;
        this.locationRepository = locationRepository;
        this.pollutionTypeRepository = pollutionTypeRepository;
    }

    /**
     * Get all available pollution types
     */
    @GetMapping("/types")
    public ResponseEntity<?> getPollutionTypes() {
        List<PollutionType> types = pollutionTypeRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PollutionType type : types) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", type.getId());
            map.put("name", type.getName());
            map.put("unit", type.getUnit());
            map.put("safeThreshold", type.getSafeThreshold());
            map.put("hazardThreshold", type.getHazardThreshold());
            map.put("description", type.getDescription());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get all monitoring locations
     */
    @GetMapping("/locations")
    public ResponseEntity<?> getLocations() {
        List<Location> locations = locationRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Location loc : locations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", loc.getId());
            map.put("name", loc.getName());
            map.put("lat", loc.getLat());
            map.put("lng", loc.getLng());
            map.put("description", loc.getDescription());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get heatmap data - readings for visualization
     * Supports filtering by pollution type and time duration
     */
    @GetMapping("/heatmap")
    public ResponseEntity<?> getHeatmapData(            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") double durationHours) {

        LocalDateTime endTime = LocalDateTime.now();
        // Convert fractional hours to seconds for Duration
        long durationSeconds = (long) (durationHours * 3600);
        LocalDateTime startTime = endTime.minusSeconds(durationSeconds);

        List<Reading> readings;
        if (typeId != null && typeId > 0) {
            readings = readingRepository.findReadingsByTypeAndTimeRange(typeId, startTime, endTime);
        } else {
            readings = readingRepository.findReadingsInTimeRange(startTime, endTime);
        }

        // Transform readings into heatmap points
        List<Map<String, Object>> heatmapPoints = new ArrayList<>();
        for (Reading reading : readings) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("id", reading.getId());
            point.put("lat", reading.getLocation().getLat());
            point.put("lng", reading.getLocation().getLng());
            point.put("value", reading.getValue());
            point.put("unit", reading.getPollutionType().getUnit());
            point.put("pollutionType", reading.getPollutionType().getName());
            point.put("locationName", reading.getLocation().getName());
            point.put("locationId", reading.getLocation().getId());
            point.put("pollutionTypeId", reading.getPollutionType().getId());
            // Use formatted timestamp to avoid DateTimeException with invalid nanoseconds
            // Use sanitized timestamp to avoid invalid nanoseconds from simulation data
            point.put("timestamp", reading.getSanitizedCreatedAt().toString());
            point.put("date", reading.getDate() != null ? reading.getDate().toString() : "");
            point.put("time", reading.getTime() != null ? reading.getTime().toString() : "");
            point.put("weather", reading.getWeather() != null ? reading.getWeather() : "Clear");
            point.put("source", reading.getSource() != null ? reading.getSource() : "SIMULATED");

            // Calculate severity based on thresholds
            PollutionType pt = reading.getPollutionType();
            double value = reading.getValue();
            String severity;
            if (value <= pt.getSafeThreshold()) {
                severity = "SAFE";
            } else if (value <= pt.getHazardThreshold()) {
                severity = "MODERATE";
            } else {
                severity = "HAZARD";
            }
            point.put("severity", severity);

            heatmapPoints.add(point);
        }

        return ResponseEntity.ok(heatmapPoints);
    }

    /**
     * Get latest readings for each location and pollution type
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestReadings(
            @RequestParam(required = false) Long typeId) {

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minus(24, ChronoUnit.HOURS); // Last 24 hours

        List<Reading> readings;
        if (typeId != null && typeId > 0) {
            readings = readingRepository.findLatestReadingsInTimeRangeByType(typeId, startTime, endTime);
        } else {
            readings = readingRepository.findLatestReadingsInTimeRange(startTime, endTime);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Reading reading : readings) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", reading.getId());
            map.put("locationId", reading.getLocation().getId());
            map.put("locationName", reading.getLocation().getName());
            map.put("lat", reading.getLocation().getLat());
            map.put("lng", reading.getLocation().getLng());
            map.put("pollutionTypeId", reading.getPollutionType().getId());
            map.put("pollutionTypeName", reading.getPollutionType().getName());
            map.put("value", reading.getValue());
            map.put("unit", reading.getPollutionType().getUnit());
            map.put("severity", getSeverity(reading));
            map.put("timestamp", reading.getCreatedAt().toString());
            map.put("description", reading.getLocation().getDescription());

            // Get suggestion for this reading
            map.put("suggestion", getSuggestionForReading(reading));

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get pollution statistics for the selected type and time range
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStatistics(            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "24") double durationHours) {

        LocalDateTime endTime = LocalDateTime.now();
        // Convert fractional hours to seconds for Duration
        long durationSeconds = (long) (durationHours * 3600);
        if (durationSeconds < 0) {
            durationSeconds = 0;
        }
        LocalDateTime startTime = endTime.minusSeconds(durationSeconds);

        // Default to first pollution type if none specified
        if (typeId == null || typeId <= 0) {
            List<PollutionType> types = pollutionTypeRepository.findAll();
            if (!types.isEmpty()) {
                typeId = types.get(0).getId();
            } else {
                return ResponseEntity.ok(Map.of("error", "No pollution types available"));
            }
        }

        List<Object[]> stats = readingRepository.getPollutionStatsByTypeAndTimeRange(typeId, startTime, endTime);

        PollutionType pollutionType = pollutionTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Pollution type not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pollutionType", pollutionType.getName());
        result.put("unit", pollutionType.getUnit());
        result.put("safeThreshold", pollutionType.getSafeThreshold());
        result.put("hazardThreshold", pollutionType.getHazardThreshold());

        if (stats.isEmpty() || stats.get(0)[0] == null) {
            result.put("average", 0.0);
            result.put("maximum", 0.0);
            result.put("minimum", 0.0);
            result.put("readingCount", 0);
            result.put("overallSeverity", "No Data");
        } else {
            Object[] stat = stats.get(0);
            double avg = ((Number) stat[0]).doubleValue();
            double max = ((Number) stat[1]).doubleValue();
            double min = ((Number) stat[2]).doubleValue();
            long count = ((Number) stat[3]).longValue();

            result.put("average", avg);
            result.put("maximum", max);
            result.put("minimum", min);
            result.put("readingCount", count);
            result.put("overallSeverity", calculateOverallSeverity(avg, pollutionType));
        }

        result.put("startTime", startTime.toString());
        result.put("endTime", endTime.toString());
        result.put("durationHours", durationHours);

        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed information for a specific reading
     */
    @GetMapping("/reading/{id}")
    public ResponseEntity<?> getReadingDetail(@PathVariable Long id) {
        return readingRepository.findById(id)
                .map(this::readingToMap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "healthy");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("database", "connected");
        status.put("area", "Ramapuram, Chennai");
        status.put("bounds", Map.of(
                "minLat", 13.010,
                "maxLat", 13.050,
                "minLng", 80.165,
                "maxLng", 80.200
        ));
        return ResponseEntity.ok(status);
    }

    /**
     * Get frontend configuration (including Google Maps API key)
     * This endpoint is public (no auth required) for the static frontend
     */
    @GetMapping("/config/maps")
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

    // Helper methods

    private Map<String, Object> readingToMap(Reading reading) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", reading.getId());
        map.put("locationId", reading.getLocation().getId());
        map.put("locationName", reading.getLocation().getName());
        map.put("lat", reading.getLocation().getLat());
        map.put("lng", reading.getLocation().getLng());
        map.put("pollutionTypeId", reading.getPollutionType().getId());
        map.put("pollutionTypeName", reading.getPollutionType().getName());
        map.put("value", reading.getValue());
        map.put("unit", reading.getPollutionType().getUnit());
        map.put("severity", getSeverity(reading));
        map.put("timestamp", reading.getCreatedAt().toString());
        map.put("date", reading.getDate().toString());
        map.put("time", reading.getTime().toString());
        map.put("weather", reading.getWeather());
        map.put("source", reading.getSource());
        map.put("description", reading.getLocation().getDescription());
        return map;
    }

    private String getSeverity(Reading reading) {
        double value = reading.getValue();
        PollutionType pt = reading.getPollutionType();
        if (value <= pt.getSafeThreshold()) {
            return "SAFE";
        } else if (value <= pt.getHazardThreshold()) {
            return "MODERATE";
        } else {
            return "HAZARD";
        }
    }

    private String calculateOverallSeverity(double avgValue, PollutionType type) {
        if (avgValue <= type.getSafeThreshold()) {
            return "SAFE";
        } else if (avgValue <= type.getHazardThreshold()) {
            return "MODERATE";
        } else {
            return "HAZARD";
        }
    }

    private Map<String, String> getSuggestionForReading(Reading reading) {
        // Simple threshold-based suggestion lookup
        double value = reading.getValue();
        PollutionType pt = reading.getPollutionType();

        if (value <= pt.getSafeThreshold()) {
            return Map.of(
                    "range", "SAFE",
                    "message", "Pollution levels are within safe limits."
            );
        } else if (value <= pt.getHazardThreshold()) {
            return Map.of(
                    "range", "MODERATE",
                    "message", "Moderate pollution levels. Sensitive groups should take precautions."
            );
        } else {
            return Map.of(
                    "range", "HAZARD",
                    "message", "HIGH POLLUTION ALERT! Take immediate precautions. Limit outdoor activities."
            );
        }
    }
}
