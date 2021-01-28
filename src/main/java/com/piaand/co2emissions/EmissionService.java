package com.piaand.co2emissions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Pageable;
import java.util.*;
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
/*
    public Pageable createPageableSort(String type, Integer amount){
        Pageable page = (Pageable) PageRequest.of(0, amount, Sort.by("year").ascending()
                        .and(Sort.by(type).descending()));
        return page;
    }
*/
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

    private void checkCategoryAndTotalSums(Double solid, Double liquid, Double gasFlaring, Double gasFuel, Double cement, Double total) {
        //Check total emissions match with sum of emissions. Bunker fuels not in total
        //This does not compute well with NaN
        Double emissionSum = solid + liquid + gasFlaring + gasFuel + cement;
        Boolean totalChecks = Math.abs(emissionSum-total) <= 1;
        if (!totalChecks) {
            logger.warning("Following total and category sum didn't match: " + total +" : " + emissionSum);
        }
    }

    //TODO: add total, per capita and bunker fuels
    private String validateType(Map<String, String> allParams) {
        List<String> validTypes = Arrays.asList("solid", "liquid", "gasflaring", "gasfuel", "cement");
        String emissionType = null;
        Boolean valid = false;

        if(allParams.containsKey("type")) {
            emissionType = allParams.get("type").toLowerCase(Locale.ROOT);
            if (validTypes.contains(emissionType)) {
                valid = true;
            }
        }
        if (valid) {
            return emissionType;
        } else {
            logger.warning("Emission type queried either null or not valid: " + emissionType);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Emission type is not in database."
            );
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
            //TODO: remember to add bunkerfuels and per capita to db

            //Handle name fromatting of of Viet Nam
            if(countryName.equals("VIET NAM")) {
                countryName = "VIETNAM";
            }

            checkCategoryAndTotalSums(solid, liquid, gasFlaring, gasFuel, cement, total);

            //Create and add emissions to country
            addCountry(countryName);
            Country country = countryRepository.findByName(countryName);
            Emission emission = new Emission(country, year, solid, liquid, gasFlaring, gasFuel, cement);
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

    public JsonNode parseEmissionResult(List<Emission> list, String type, Integer limit) {
        try {
            if (list.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No data with these parameters."
                );
            }
            Integer groupYear = list.get(0).getYear();
            System.out.println(groupYear);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            ObjectNode endResult = mapper.createObjectNode();
            Integer size = list.size();
            Integer i = 0;

            for (Emission emission : list) {
                ArrayNode arrayNode = mapper.createArrayNode();
                JsonNode node = mapper.convertValue(emission, JsonNode.class);
                arrayNode.add(node);
                if(emission.getYear() != groupYear || i == size - 1) {
                    ObjectNode polluterResultNode = mapper.createObjectNode();
                    polluterResultNode.set("polluters", arrayNode);
                    polluterResultNode.put("year", groupYear);
                    endResult.set("result", polluterResultNode);
                    groupYear = emission.getYear();

                }
                i++;
            }
            System.out.println(endResult.toString());
            return endResult;
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            throw new ResponseStatusException(
                    HttpStatus.NOT_IMPLEMENTED, "Parsing result is not acting as planned."
            );
        }
    }

    public JsonNode listWorstPollutersWithFilters(Map<String, String> allParams) {
        String emissionType = null;
        //the earliest year we have data on;
        Integer from = 1751;
        //latest year we have data on
        Integer to = 2021;
        Integer top = 0;

        try {
            //Insert all query params
            emissionType = validateType(allParams);
            if (allParams.containsKey("from")) {
                from = parseYearToInt(allParams.get("from"));
            }
            if (allParams.containsKey("to")) {
                to = parseYearToInt(allParams.get("to"));
            }
            if (allParams.containsKey("top")) {
                top = Integer.parseInt(allParams.get("top"));
            }

            //Pageable page = createPageableSort(emissionType, top);
            System.out.println("Now search");
            //TODO: if total, then other repository query
            List<Emission> polluters = emissionRepository.findAllByYearBetweenOrderByYearAsc(from, to);
            System.out.println("print polluter nro 1");

            return parseEmissionResult(polluters, emissionType, top);
        } catch (NumberFormatException e) {
            logger.warning("Query params were not in number format.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query params were not in number format.");
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
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
