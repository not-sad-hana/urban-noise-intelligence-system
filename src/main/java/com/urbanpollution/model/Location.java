package com.urbanpollution.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Location name is required")
    @Column(nullable = false, length = 150)
    private String name;

    // Bounding box lat: 13.010 - 13.050
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "13.010000", message = "Latitude must be within Ramapuram (min: 13.010)")
    @DecimalMax(value = "13.050000", message = "Latitude must be within Ramapuram (max: 13.050)")
    @Column(nullable = false, columnDefinition = "DECIMAL(9, 6)")
    private Double lat;

    // Bounding box lng: 80.165 - 80.200
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "80.165000", message = "Longitude must be within Ramapuram (min: 80.165)")
    @DecimalMax(value = "80.200000", message = "Longitude must be within Ramapuram (max: 80.200)")
    @Column(nullable = false, columnDefinition = "DECIMAL(9, 6)")
    private Double lng;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Location() {
    }

    public Location(String name, Double lat, Double lng, String description) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    public void validateAreaLock() {
        if (lat == null || lat < 13.010 || lat > 13.050) {
            throw new IllegalArgumentException("Latitude " + lat + " is outside Ramapuram bounding box [13.010, 13.050]");
        }
        if (lng == null || lng < 80.165 || lng > 80.200) {
            throw new IllegalArgumentException("Longitude " + lng + " is outside Ramapuram bounding box [80.165, 80.200]");
        }
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
