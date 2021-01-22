package com.piaand.co2emissions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.logging.Logger;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class Co2EmissionsApplicationTests {

	private static final Logger LOGGER = Logger.getLogger(Co2EmissionsApplication.class.getName());
	private static final String logFileName = "./logs/testEmissions.log";
	private static final File logFile = new File(logFileName);

	@Autowired
	LogConfigurations config;

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
	}

	@AfterEach
	public void cleanTest() {
		LOGGER.info("Tearing down db entries after test.");
	}

	@Test
	void contextLoads() {
	}

}
