package com.piaand.co2emissions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class EmissionService {
    private static final Logger logger = Logger.getLogger(Co2EmissionsApplication.class.getName());
    private final EmissionRepository emissionRepository;
    private final CountryRepository countryRepository;

    public EmissionService(EmissionRepository emissionRepository, CountryRepository countryRepository) {
        this.emissionRepository = emissionRepository;
        this.countryRepository = countryRepository;
    }

    private Double parseEmissionsToDouble(String emissionField) {
        try{
            Double emissions;
            if (emissionField == null || emissionField.isBlank()) {
                emissions = Double.NaN;
            } else {
                emissions = Double.parseDouble(emissionField);
            }
            return emissions;
        } catch (NumberFormatException e) {
            logger.warning("Emission field: " + emissionField + " cannot be transformed to number.");
            return Double.NaN;
        }
    }

    private Integer parseYearToInt(String yearField) {
        try {
            Integer year = Integer.parseInt(yearField);
            return year;
        } catch (NumberFormatException e) {
            logger.warning("Year field: " + yearField + " cannot be transformed to number.");
            throw new NumberFormatException("Data row is corrupted.");
        }
    }

    public Emission createDataObjectFromRow(String[] dataRow) {
        try {
            Integer year = parseYearToInt(dataRow[0]);
            String countryName = dataRow[1];
            Double total = parseEmissionsToDouble(dataRow[2]);
            Double solid = parseEmissionsToDouble(dataRow[3]);
            Double liquid = parseEmissionsToDouble(dataRow[4]);
            Double gasFuel = parseEmissionsToDouble(dataRow[5]);
            Double cement = parseEmissionsToDouble(dataRow[6]);
            Double gasFlaring = parseEmissionsToDouble(dataRow[7]);

            //Handle name fromatting of of Viet Nam
            if(countryName.equals("VIET NAM")) {
                countryName = "VIETNAM";
            }

            //Check total emissions match with sum of emissions. Bunker fuels not in total
            Double emissionSum = solid + liquid + gasFlaring + gasFuel + cement;
            Boolean totalChecks = Math.abs(emissionSum-total) <= 1;
            if (!totalChecks) {
                logger.warning("Following total and category sum didn't match: " + total +" : " + emissionSum);
            }

            //Create and add emissions to country
            addCountry(countryName);
            Country country = countryRepository.findByName(countryName);
            Emission emission = new Emission(country, year);
            return emission;
        } catch (NumberFormatException e) {
            logger.warning("Year field: " + dataRow[0] + " cannot be transformed to number.");
            throw new RuntimeException("Data row is corrupted.");
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            return null;
        }
    }

    public void addCountry(String countryName) {
        Country country = new Country();
        country.setName(countryName);
        try {
            saveCountryToDatabase(country);
            logger.info("Added a new country: " + countryName + " to database.");
        } catch (DataIntegrityViolationException e) {
            //Country is already listed in the database or other requirements are not met
        } catch (Exception e) {
            logger.warning("Couldn't add a new country: " + countryName + " error: " + e);
        }
    }

    public JsonNode listPollutersAlphabetically() {
        List<Country> polluterRows = countryRepository.findAllByOrderByName();
        List<String> polluters = new ArrayList<>();
        for (Country polluter : polluterRows) {
            String name = polluter.getName();
            polluters.add(name);
        }
        JsonNode result = turnStringListToJson(polluters);
        return result;
    }

    public ObjectNode turnStringListToJson(List<String> names) {
        if (names.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No name is found"
            );
        } else {
            try {
                logger.info("Start parsing string list to json array");
                ObjectMapper mapper = new ObjectMapper();
                ArrayNode arrayNode = mapper.createArrayNode();
                for (String name : names) {
                    JsonNode node = mapper.convertValue(name, JsonNode.class);
                    arrayNode.add(node);
                }
                ObjectNode resultNode = mapper.createObjectNode();
                resultNode.set("data", arrayNode);
                return resultNode;
            } catch (Exception e) {
                logger.severe("Cannot return string list as json. Error: " + e.getMessage());
                throw new ResponseStatusException(
                        HttpStatus.NOT_IMPLEMENTED, "Result cannot be returned."
                );
            }
        }
    }

    public ObjectNode listEndPoints() {
        String endPoint1 = "/polluters - returns all polluter countries alphabetically sorted";

        List<String> list = new ArrayList<>();
        list.add(endPoint1);
        return turnStringListToJson(list);
    }

    public void saveEmissionToDatabase(Emission emission) {
        emissionRepository.save(emission);
    }

    public void saveCountryToDatabase(Country country) {
        countryRepository.save(country);
    }
}
