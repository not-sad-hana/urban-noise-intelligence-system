package com.urbanpollution.repository;

import com.urbanpollution.model.PollutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollutionTypeRepository extends JpaRepository<PollutionType, Long> {
    Optional<PollutionType> findByNameIgnoreCase(String name);
}
