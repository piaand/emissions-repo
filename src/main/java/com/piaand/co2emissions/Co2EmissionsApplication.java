package com.piaand.co2emissions;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.*;
import java.util.logging.Logger;


/**
 * CO2 emission interface - application builds an API interface
 * @author Pia Andersin
 */
@SpringBootApplication
public class Co2EmissionsApplication {

	private final LogConfigurations configurations;
	private final InputChecker inputChecker;
	private static final Logger logger = Logger.getLogger(Co2EmissionsApplication.class.getName());
	private static final String pathToResource = "src/main/resources";
	private static final String filePathDefault = "/csv/emissions.csv";

	public Co2EmissionsApplication(LogConfigurations configurations, InputChecker inputChecker) {
		this.configurations = configurations;
		this.inputChecker = inputChecker;
	}


	private final void readCsvEmissions() {
		File inputFile = new File(pathToResource + filePathDefault);
		if (inputFile.isFile() && inputFile.exists()) {
			try {
				BufferedReader csvReader = new BufferedReader(new FileReader(pathToResource + filePathDefault));
				String row;
				while ((row = csvReader.readLine()) != null) {
					String[] data = row.split(",");
					inputChecker.addEmissionToDatabase(data);
				}
				csvReader.close();
			} catch (FileNotFoundException e) {
				logger.severe("File not found at: " + pathToResource + filePathDefault + " system exits.");
				System.exit(1);
			} catch	(Exception e) {
				logger.severe("Unexpected error:" + e + " system exits.");
				System.exit(1);
			}

		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Co2EmissionsApplication.class, args);
	}

	@Profile("!test")
	@Bean
	CommandLineRunner runner() {
		return args -> {
			configurations.setConfig();
			readCsvEmissions();
		};
	}

}
