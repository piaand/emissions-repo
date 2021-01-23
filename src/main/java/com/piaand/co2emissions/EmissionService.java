package com.piaand.co2emissions;

import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmissionService {
    private static final Logger logger = Logger.getLogger(Co2EmissionsApplication.class.getName());
    private final EmissionRepository emissionRepository;

    public EmissionService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    public Emission createDataObjectFromRow(String[] dataRow) {
        try {
            Integer year = Integer.parseInt(dataRow[0].replace("\"", ""));
            Emission emission = new Emission(dataRow[1], year);
            return emission;
        } catch (NumberFormatException e) {
            logger.severe("Year field: " + dataRow[0] + " cannot be transformed to number.");
            throw new RuntimeException("Data row is corrupted.");
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            return null;
        }
    }

    public void saveToDatabase(Emission emission) {
        emissionRepository.save(emission);
    }
}
