package com.urbanpollution.config;

import com.urbanpollution.service.SimulationDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes simulation pollution data on application startup.
 * Generates data for the last 24 hours for realistic heatmap visualization.
 */
@Configuration
public class SimulationDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SimulationDataInitializer.class);

    @Bean
    public CommandLineRunner initializeSimulationData(SimulationDataService simulationDataService) {
        return args -> {
            logger.info("=================================================================");
            logger.info("Initializing Simulation Pollution Data");
            logger.info("=================================================================");

            try {
                // Generate 24 hours of simulation data with 4 readings per hour per location
                // This gives us 24 * 4 * 6 locations * 2 pollution types = 1152 readings
                simulationDataService.generateSimulationData(24, 4);

                logger.info("Simulation data initialization COMPLETE");
            } catch (Exception e) {
                logger.error("Failed to initialize simulation data: {}", e.getMessage(), e);
            }

            logger.info("=================================================================");
        };
    }
}
