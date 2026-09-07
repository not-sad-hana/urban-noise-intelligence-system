package com.urbanpollution.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Database Connection and Health Verification Configuration.
 * Validates JDBC connection to PostgreSQL on application startup and logs connection metadata.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner verifyDatabaseConnection(DataSource dataSource) {
        return args -> {
            logger.info("=================================================================");
            logger.info("Urban Pollution Monitoring MVP - Verifying PostgreSQL JDBC Connection");
            logger.info("=================================================================");
            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    DatabaseMetaData metaData = connection.getMetaData();
                    logger.info("PostgreSQL Connection SUCCESSFUL!");
                    logger.info("Database Product Name    : {}", metaData.getDatabaseProductName());
                    logger.info("Database Product Version : {}", metaData.getDatabaseProductVersion());
                    logger.info("Driver Name              : {}", metaData.getDriverName());
                    logger.info("Driver Version           : {}", metaData.getDriverVersion());
                    logger.info("JDBC URL                 : {}", metaData.getURL());
                    logger.info("Active User              : {}", metaData.getUserName());
                    logger.info("Area Lock Active         : Ramapuram (Lat 13.010-13.050, Lng 80.165-80.200)");
                }
            } catch (SQLException e) {
                logger.error("Failed to connect to PostgreSQL database! Please verify PostgreSQL service is running.", e);
            }
            logger.info("=================================================================");
        };
    }
}
