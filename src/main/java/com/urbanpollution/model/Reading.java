package com.urbanpollution.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "readings", indexes = {
    @Index(name = "idx_readings_loc_type_date", columnList = "location_id, type_id, reading_date, reading_time")
})
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private PollutionType pollutionType;

    @Column(nullable = false)
    private Double value;

    @Column(name = "reading_date", nullable = false)
    private LocalDate date;

    @Column(name = "reading_time", nullable = false)
    private LocalTime time;

    @Column(length = 50)
    private String weather = "Clear";

    @Column(nullable = false, length = 50)
    private String source = "SIMULATED";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Safe getter that truncates invalid nanoseconds from simulation data.
     */
    public LocalDateTime getSanitizedCreatedAt() {
        if (createdAt == null) {
            return LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        }
        try {
            return createdAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        } catch (Exception e) {
            // If truncation fails due to invalid nanoseconds, create a new safe timestamp
            return LocalDateTime.of(createdAt.toLocalDate(), createdAt.toLocalTime().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        }
    }

    public Reading() {
    }

    public Reading(Location location, PollutionType pollutionType, Double value, LocalDate date, LocalTime time, String weather, String source) {
        this.location = location;
        this.pollutionType = pollutionType;
        this.value = value;
        this.date = date;
        this.time = time;
        this.weather = weather != null ? weather : "Clear";
        this.source = source != null ? source : "SIMULATED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public PollutionType getPollutionType() {
        return pollutionType;
    }

    public void setPollutionType(PollutionType pollutionType) {
        this.pollutionType = pollutionType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
