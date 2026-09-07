package com.urbanpollution.repository;

import com.urbanpollution.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByPollutionTypeId(Long typeId);

    // Find suggestion where value falls within [minValue, maxValue]
    @Query("SELECT s FROM Suggestion s WHERE s.pollutionType.id = :typeId AND :value >= s.minValue AND :value <= s.maxValue")
    Optional<Suggestion> findMatchingSuggestion(@Param("typeId") Long typeId, @Param("value") Double value);
}
