package com.urbanpollution.repository;

import com.urbanpollution.model.Reading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    List<Reading> findByLocationId(Long locationId);

    List<Reading> findByPollutionTypeId(Long typeId);

    List<Reading> findByLocationIdAndPollutionTypeIdOrderByDateAscTimeAsc(Long locationId, Long typeId);

    List<Reading> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Latest N readings for a specific location and pollution type (used for Hotspot detection)
    @Query("SELECT r FROM Reading r WHERE r.location.id = :locationId AND r.pollutionType.id = :typeId ORDER BY r.date DESC, r.time DESC")
    List<Reading> findLatestNReadings(
        @Param("locationId") Long locationId,
        @Param("typeId") Long typeId,
        Pageable pageable
    );

    // Get the very latest reading for each location
    @Query("SELECT r FROM Reading r WHERE r.id IN (" +
           "  SELECT MAX(sub.id) FROM Reading sub GROUP BY sub.location.id, sub.pollutionType.id" +
           ")")
    List<Reading> findLatestReadingsGrouped();

    // Get latest readings filtered by pollution type and time range
    @Query("SELECT r FROM Reading r WHERE r.pollutionType.id = :typeId " +
           "AND r.createdAt >= :startTime AND r.createdAt <= :endTime " +
           "ORDER BY r.createdAt DESC")
    List<Reading> findLatestReadingsByTypeAndTimeRange(
        @Param("typeId") Long typeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Get all readings within a time range for heatmap data
    @Query("SELECT r FROM Reading r WHERE r.createdAt >= :startTime AND r.createdAt <= :endTime " +
           "ORDER BY r.createdAt DESC")
    List<Reading> findReadingsInTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Get readings filtered by pollution type and time range for heatmap
    @Query("SELECT r FROM Reading r WHERE r.pollutionType.id = :typeId " +
           "AND r.createdAt >= :startTime AND r.createdAt <= :endTime " +
           "ORDER BY r.createdAt DESC")
    List<Reading> findReadingsByTypeAndTimeRange(
        @Param("typeId") Long typeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Get statistics for a pollution type within time range
    @Query("SELECT AVG(r.value) as avgValue, MAX(r.value) as maxValue, MIN(r.value) as minValue, COUNT(r) as count " +
           "FROM Reading r WHERE r.pollutionType.id = :typeId " +
           "AND r.createdAt >= :startTime AND r.createdAt <= :endTime")
    List<Object[]> getPollutionStatsByTypeAndTimeRange(
        @Param("typeId") Long typeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Get latest reading for each location and pollution type combination
    @Query("SELECT r FROM Reading r WHERE r.id IN (" +
           "  SELECT MAX(sub.id) FROM Reading sub " +
           "  WHERE sub.createdAt >= :startTime AND sub.createdAt <= :endTime " +
           "  GROUP BY sub.location.id, sub.pollutionType.id" +
           ")")
    List<Reading> findLatestReadingsInTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Get latest reading for each location and pollution type filtered by type
    @Query("SELECT r FROM Reading r WHERE r.id IN (" +
           "  SELECT MAX(sub.id) FROM Reading sub " +
           "  WHERE sub.pollutionType.id = :typeId " +
           "  AND sub.createdAt >= :startTime AND sub.createdAt <= :endTime " +
           "  GROUP BY sub.location.id, sub.pollutionType.id" +
           ")")
    List<Reading> findLatestReadingsInTimeRangeByType(
        @Param("typeId") Long typeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
