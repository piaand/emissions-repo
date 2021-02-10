package com.piaand.co2emissions;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Co2Application with in-memory database. Commandline runner that reads all the data from csv is ommitted.
 * Database initiated instead at the beginning of every test.
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class Co2EmissionsApplicationTests {

	private static final Logger LOGGER = Logger.getLogger(Co2EmissionsApplication.class.getName());
	private static final String logFileName = "./logs/testEmissions.log";
	private static final File logFile = new File(logFileName);
	private final String countryName1 = "Finland";
	private final String countryName2 = "Sweden";
	private final String countryName3 = "Norway";
	private final String countryName4 = "Russia";
	private final Integer year1 = 1991;
	private final Integer year2 = 1980;
	private final Integer year3 = 2000;
	private final Integer year4 = 2020;
	private final Double solid1 = 0.0;
	private final Double solid2 = 0.0;
	private final Double solid3 = 100.9;
	private final Double solid4 = 10.1;
	private final Double liquid1 = 29.2;
	private final Double liquid2 = 0.0;
	private final Double liquid3 = 0.0;
	private final Double liquid4 = 1.0;
	private final Double gasFlaring = 1.0;
	private final Double gasFuels = 1.0;
	private final Double cement = 1.0;
	private final Double perCapita1 = 1.0;
	private final Double perCapita2 = 7.0;
	private final Double bunkerFuels1 = 8.0;
	private final Double bunkerFuels2 = 100.90;


	private void saveCountry(String name) {
		Country country	= new Country();
		country.setName(name);
		countryRepository.save(country);
	}

	private void addInitCountries() {
		saveCountry(countryName1);
		saveCountry(countryName2);
		saveCountry(countryName3);
		saveCountry(countryName4);
	}

	private void saveEmission(String countryName, Integer year, Double solid, Double liquid, Double gasFlaring, Double gasFuels, Double cement, Double perCapita, Double bunkerFuels) {
		Country country = countryRepository.findByName(countryName);
		Emission emission = new Emission(country, year, solid, liquid, gasFlaring, gasFuels, cement, perCapita, bunkerFuels);
		emissionRepository.save(emission);
	}

	private void addInitEmissions() {
		saveEmission(countryName1, year1, solid1, liquid1, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName1, year2, solid1, liquid2, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName1, year3, solid2, liquid1, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName1, year4, solid2, liquid3, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName2, year1, solid4, liquid4, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName2, year2, solid3, liquid1, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName2, year3, solid3, liquid4, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName2, year4, solid3, liquid2, gasFlaring, gasFuels, cement, perCapita1, bunkerFuels1);
		saveEmission(countryName3, year1, solid3, liquid3, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName3, year2, solid1, liquid3, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName3, year3, solid1, liquid4, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName3, year4, solid2, liquid2, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName4, year1, solid2, liquid4, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName4, year2, solid4, liquid1, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName4, year3, solid4, liquid2, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);
		saveEmission(countryName4, year4, solid4, liquid3, gasFlaring, gasFuels, cement, perCapita2, bunkerFuels2);

	}

	@Autowired
	LogConfigurations config;

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	EmissionService emissionService;

	@Autowired
	EmissionRepository emissionRepository;

	@BeforeAll
	static void init() {
		if (logFile.exists()) {
			logFile.delete();
		}
	}

	@BeforeEach
	public void initTest() {
		config.setConfig();
		LOGGER.info("Saving db entries for next test.");
		addInitCountries();
		addInitEmissions();
	}

	@AfterEach
	public void cleanTest() {
		LOGGER.info("Tearing down db entries after test.");
		emissionRepository.deleteAll();
		countryRepository.deleteAll();
	}

	@Test
	public void testGetCountries() {
		LOGGER.info("Run test testGetCountries");
		List<Country> listCountries = countryRepository.findAllByOrderByName();
		assertFalse(listCountries.isEmpty());

		if(listCountries.size() > 1) {
			JsonNode node = emissionService.listPollutersAlphabetically().get("data");
			String firstCountry = node.get(0).toString();
			String secondCountry = node.get(1).toString();

			assertTrue(firstCountry.compareTo(secondCountry) <= 0);
		}
	}

	@Test
	public void testGetCountriesNotFound() {
		LOGGER.info("Run test testGetCountriesNotFound");
		emissionRepository.deleteAll();
		countryRepository.deleteAll();
		List<Country> listCountry = countryRepository.findAllByOrderByName();
		assertTrue(listCountry.isEmpty());
		Assertions.assertThrows(ResponseStatusException.class, () -> {
			JsonNode node = emissionService.listPollutersAlphabetically();
		});
	}

	@Test
	void contextLoads() {
	}

}
