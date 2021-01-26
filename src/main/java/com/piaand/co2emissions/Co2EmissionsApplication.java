package com.piaand.co2emissions;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
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

	private final CSVReader readData() {
		try {
			final CSVParser parser = new CSVParserBuilder()
					.withSeparator(',')
					.withIgnoreQuotations(false)
					.build();
			final CSVReader reader = new CSVReaderBuilder(new FileReader(pathToResource + filePathDefault))
					.withSkipLines(1)
					.withCSVParser(parser)
					.build();
			return reader;
		} catch (FileNotFoundException e) {
			logger.severe("File not found at: " + pathToResource + filePathDefault + " system exits.");
			throw new RuntimeException("Error reading csv file");
		} catch	(Exception e) {
			logger.severe("Unexpected error:" + e + " when creating reader. System exits.");
			throw new RuntimeException("Error reading csv file");
		}
	}

	private final void readCsvEmissions() {
		File inputFile = new File(pathToResource + filePathDefault);
		if (inputFile.isFile() && inputFile.exists()) {
			try {
				CSVReader reader = readData();
				String[] row;
				while ((row = reader.readNext()) != null) {
					inputChecker.addEmissionToDatabase(row);
				}
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
