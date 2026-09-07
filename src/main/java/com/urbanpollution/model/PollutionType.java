package com.urbanpollution.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pollution_type")
public class PollutionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // e.g., NOISE, AIR_QUALITY (extensible to WATER, SOIL, LIGHT)

    @Column(nullable = false, length = 20)
    private String unit; // e.g., dB, AQI

    @Column(name = "safe_threshold", nullable = false)
    private Double safeThreshold;

    @Column(name = "hazard_threshold", nullable = false)
    private Double hazardThreshold;

    @Column(length = 255)
    private String description;

    public PollutionType() {
    }

    public PollutionType(String name, String unit, Double safeThreshold, Double hazardThreshold, String description) {
        this.name = name;
        this.unit = unit;
        this.safeThreshold = safeThreshold;
        this.hazardThreshold = hazardThreshold;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getSafeThreshold() {
        return safeThreshold;
    }

    public void setSafeThreshold(Double safeThreshold) {
        this.safeThreshold = safeThreshold;
    }

    public Double getHazardThreshold() {
        return hazardThreshold;
    }

    public void setHazardThreshold(Double hazardThreshold) {
        this.hazardThreshold = hazardThreshold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
