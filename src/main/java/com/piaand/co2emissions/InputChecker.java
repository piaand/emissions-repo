package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

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
        setRowCounter(getRowCounter() + 1);
        if (getRowCounter() == 1) {
            logger.info("Read the header row.");
        } else {
            try {
                String emissionYear = data[0];
                String emissionCountry = data[1];
                if (emissionYear == null || emissionYear.isBlank() || emissionCountry == null || emissionCountry.isBlank()) {
                    //TODO: add corrupted rows to own table
                    logger.warning("Data row number " + rowCounter + " in csv has either no year or no country.");
                } else {
                    Emission emission = emissionService.createDataObjectFromRow(data);
                    emissionService.saveToDatabase(emission);
                }
            } catch (RuntimeException e) {
                logger.warning("Data row number " + rowCounter + " cannot be added. Reason: " + e);
            } catch (Exception e) {
                logger.severe("Unexpected exception: " + e);
            }
        }

    }
}
