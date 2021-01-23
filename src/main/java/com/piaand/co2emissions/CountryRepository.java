package com.piaand.co2emissions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA repository interface for polluter countries that does all the searches from database.
 */
public interface CountryRepository extends JpaRepository<Country, Long> {
    Country findByName(String name);
    List<Country> findAllByOrderByName();
}
