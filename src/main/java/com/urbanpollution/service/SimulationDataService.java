package com.urbanpollution.service;

import com.urbanpollution.model.Location;
import com.urbanpollution.model.PollutionType;
import com.urbanpollution.model.Reading;
import com.urbanpollution.repository.LocationRepository;
import com.urbanpollution.repository.PollutionTypeRepository;
import com.urbanpollution.repository.ReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Service to generate simulated pollution data for demonstration purposes.
 * Generates realistic pollution readings for Ramapuram, Chennai locations.
 */
@Service
public class SimulationDataService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationDataService.class);

    private final ReadingRepository readingRepository;
    private final LocationRepository locationRepository;
    private final PollutionTypeRepository pollutionTypeRepository;

    // Base pollution levels for different location types (PM2.5 equivalent values)
    private static final double[] COMMERCIAL_BASELINE = {85, 95, 110, 90};  // Busy commercial areas
    private static final double[] RESIDENTIAL_BASELINE = {45, 55, 60, 40};  // Residential areas
    private static final double[] INDUSTRIAL_BASELINE = {120, 140, 160, 130}; // Industrial zones
    private static final double[] PARK_BASELINE = {25, 35, 30, 20};  // Green areas

    private final Random random = new Random();

    public SimulationDataService(ReadingRepository readingRepository,
                                  LocationRepository locationRepository,
                                  PollutionTypeRepository pollutionTypeRepository) {
        this.readingRepository = readingRepository;
        this.locationRepository = locationRepository;
        this.pollutionTypeRepository = pollutionTypeRepository;
    }

    /**
     * Generate simulated pollution readings for the last N hours
     * @param hoursBack Number of hours to generate data for
     * @param readingsPerHour Number of readings to generate per hour per location
     */
    @Transactional
    public void generateSimulationData(int hoursBack, int readingsPerHour) {
        logger.info("Generating simulation data for last {} hours with {} readings/hour...", hoursBack, readingsPerHour);

        List<Location> locations = locationRepository.findAll();
        List<PollutionType> pollutionTypes = pollutionTypeRepository.findAll();

        if (locations.isEmpty()) {
            logger.warn("No locations found. Cannot generate simulation data.");
            return;
        }

        if (pollutionTypes.isEmpty()) {
            logger.warn("No pollution types found. Cannot generate simulation data.");
            return;
        }

        int totalReadings = 0;
        LocalDateTime currentTime = LocalDateTime.now().minusHours(hoursBack);

        // Clear existing readings older than the simulation period
        LocalDateTime cutoff = currentTime;
        // Note: We don't delete here to preserve data, just add new readings

        for (int hour = 0; hour < hoursBack * readingsPerHour; hour++) {
            LocalDateTime readingTime = currentTime.plusHours(hour / readingsPerHour)
                    .plusMinutes((hour % readingsPerHour) * (60 / readingsPerHour));

            for (Location location : locations) {
                for (PollutionType pollutionType : pollutionTypes) {
                    // Skip if we already have a recent reading for this combination
                    // (In production, you'd check this more carefully)

                    double value = generatePollutionValue(location, pollutionType, readingTime);
                    // Round to milliseconds to avoid invalid nanosecond values
                    LocalDateTime safeTime = readingTime.truncatedTo(ChronoUnit.MILLIS);
                    Reading reading = new Reading(
                            location,
                            pollutionType,
                            value,
                            safeTime.toLocalDate(),
                            safeTime.toLocalTime(),
                            generateWeather(),
                            "SIMULATED"
                    );
                    reading.setCreatedAt(safeTime);
                    readingRepository.save(reading);
                    totalReadings++;
                }
            }
        }

        logger.info("Generated {} simulation readings", totalReadings);
    }

    /**
     * Generate pollution value based on location type and time of day
     */
    private double generatePollutionValue(Location location, PollutionType pollutionType, LocalDateTime time) {
        double baseValue = getBaseValueForLocation(location);
        double hourlyVariation = getHourlyVariation(time);
        double randomVariation = (random.nextDouble() - 0.5) * 20; // ±10 random variation

        double value = baseValue + hourlyVariation + randomVariation;

        // Ensure value is positive
        value = Math.max(5, value);

        // Scale based on pollution type
        if ("NOISE".equalsIgnoreCase(pollutionType.getName())) {
            // Noise levels: 30-100 dB range
            value = 30 + (value / 150) * 70; // Scale to noise range
            value = Math.max(30, Math.min(100, value));
        } else if ("AIR_QUALITY".equalsIgnoreCase(pollutionType.getName())) {
            // AQI: 0-500 range
            value = (value / 150) * 400; // Scale to AQI range
            value = Math.max(0, Math.min(500, value));
        }

        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Determine base pollution value based on location name/description
     */
    private double getBaseValueForLocation(Location location) {
        String name = location.getName().toLowerCase();
        String desc = location.getDescription().toLowerCase();

        // Commercial/business areas - higher pollution
        if (name.contains("bazaar") || name.contains("market") ||
            name.contains("cybercity") || name.contains("gate") ||
            desc.contains("commercial") || desc.contains("traffic") ||
            desc.contains("vehicular")) {
            return COMMERCIAL_BASELINE[random.nextInt(COMMERCIAL_BASELINE.length)];
        }

        // Hospital/medical areas - moderate
        if (name.contains("hospital") || name.contains("miot")) {
            return RESIDENTIAL_BASELINE[random.nextInt(RESIDENTIAL_BASELINE.length)] + 10;
        }

        // Educational institutions - moderate
        if (name.contains("campus") || name.contains("ist") || name.contains("srm")) {
            return RESIDENTIAL_BASELINE[random.nextInt(RESIDENTIAL_BASELINE.length)] + 5;
        }

        // Transit hubs - higher
        if (name.contains("bus") || name.contains("terminal") || name.contains("junction")) {
            return COMMERCIAL_BASELINE[random.nextInt(COMMERCIAL_BASELINE.length)] - 10;
        }

        // Parks/green areas - lower
        if (name.contains("park") || name.contains("lake") || name.contains("eco")) {
            return PARK_BASELINE[random.nextInt(PARK_BASELINE.length)];
        }

        // Default to residential baseline
        return RESIDENTIAL_BASELINE[random.nextInt(RESIDENTIAL_BASELINE.length)];
    }

    /**
     * Get pollution variation based on time of day
     * Higher during rush hours, lower at night
     */
    private double getHourlyVariation(LocalDateTime time) {
        int hour = time.getHour();

        // Rush hour peaks: 8-10 AM and 5-8 PM
        if (hour >= 8 && hour <= 10) {
            return 20 + (random.nextDouble() * 10); // Morning rush
        }
        if (hour >= 17 && hour <= 20) {
            return 25 + (random.nextDouble() * 15); // Evening rush
        }

        // Night time: lower pollution
        if (hour >= 22 || hour <= 5) {
            return -15 + (random.nextDouble() * 10); // Reduced activity
        }

        // Midday: moderate
        if (hour >= 11 && hour <= 14) {
            return 5 + (random.nextDouble() * 10);
        }

        // Early morning/late evening: moderate-low
        return -5 + (random.nextDouble() * 10);
    }

    /**
     * Generate random weather condition
     */
    private String generateWeather() {
        String[] weathers = {"Clear", "Clear", "Cloudy", "Cloudy", "Partly Cloudy", "Hazy"};
        return weathers[random.nextInt(weathers.length)];
    }

    /**
     * Generate a single reading for testing/demo
     */
    @Transactional
    public Reading generateSingleReading(Location location, PollutionType pollutionType) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        double value = generatePollutionValue(location, pollutionType, now);

        Reading reading = new Reading(
                location,
                pollutionType,
                value,
                now.toLocalDate(),
                now.toLocalTime(),
                generateWeather(),
                "SIMULATED"
        );
        reading.setCreatedAt(now);
        return reading;
    }
}
