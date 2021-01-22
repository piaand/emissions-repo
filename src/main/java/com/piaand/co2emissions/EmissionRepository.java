package com.piaand.co2emissions;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository interface that does all the searches from database.
 */
public interface EmissionRepository extends JpaRepository<Emission, Long> {
}
