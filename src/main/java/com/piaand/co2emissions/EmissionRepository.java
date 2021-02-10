package com.piaand.co2emissions;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;

/**
 * JPA repository interface that does all the searches from database.
 */
public interface EmissionRepository extends JpaRepository<Emission, Long> {
    List<Emission> findAllByYearBetweenOrderByYearAsc(Integer from, Integer to);
}
