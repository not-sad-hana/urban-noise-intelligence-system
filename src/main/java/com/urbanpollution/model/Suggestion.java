package com.urbanpollution.model;

import jakarta.persistence.*;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private PollutionType pollutionType;

    @Column(name = "threshold_range", nullable = false, length = 50)
    private String thresholdRange; // SAFE, MODERATE, HAZARD

    @Column(name = "min_value", nullable = false)
    private Double minValue;

    @Column(name = "max_value", nullable = false)
    private Double maxValue;

    @Column(name = "recommendation_text", nullable = false, columnDefinition = "TEXT")
    private String recommendationText;

    public Suggestion() {
    }

    public Suggestion(PollutionType pollutionType, String thresholdRange, Double minValue, Double maxValue, String recommendationText) {
        this.pollutionType = pollutionType;
        this.thresholdRange = thresholdRange;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.recommendationText = recommendationText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PollutionType getPollutionType() {
        return pollutionType;
    }

    public void setPollutionType(PollutionType pollutionType) {
        this.pollutionType = pollutionType;
    }

    public String getThresholdRange() {
        return thresholdRange;
    }

    public void setThresholdRange(String thresholdRange) {
        this.thresholdRange = thresholdRange;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getRecommendationText() {
        return recommendationText;
    }

    public void setRecommendationText(String recommendationText) {
        this.recommendationText = recommendationText;
    }
}
