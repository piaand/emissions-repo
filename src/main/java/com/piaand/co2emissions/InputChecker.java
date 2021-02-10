package com.piaand.co2emissions;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Class to validate and save the read data to the database. Run by the commandline runner.
 */
@Data
@Component
public class InputChecker {
    private static final Logger logger = Logger.getLogger(Co2EmissionsApplication.class.getName());
    private final EmissionService emissionService;
    private Long rowCounter;

    public InputChecker(EmissionService emissionService) {
        this.emissionService = emissionService;
        this.rowCounter = 0L;
    }

    public void addEmissionToDatabase(String[] data) {
        //CSV reader skips the header row, counting follows csv row numbers
        setRowCounter(getRowCounter() + 2);
        try {
            String emissionYear = data[0];
            String emissionCountry = data[1];
            if (emissionYear == null || emissionYear.isBlank() || emissionCountry == null || emissionCountry.isBlank()) {
                logger.warning("Data row number " + rowCounter + " in csv has either no year or no country.");
            } else {
                Emission emission = emissionService.createDataObjectFromRow(data);
                emissionService.saveEmissionToDatabase(emission);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //TODO: "BONAIRE, SAINT EUSTATIUS, AND SABA" with double quotes are catch here
            logger.warning("Data string at index 0: " + data[0]);
        } catch (RuntimeException e) {
            logger.warning("Data row number " + rowCounter + " cannot be added. Reason: " + e);
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
        }
    }
}
