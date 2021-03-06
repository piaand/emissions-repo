package com.piaand.co2emissions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service layer that contains all the logic of the API app.
 */
@Service
public class EmissionService {
    private static final Logger logger = Logger.getLogger(Co2EmissionsApplication.class.getName());
    private final EmissionRepository emissionRepository;
    private final CountryRepository countryRepository;
    private final EmissionData emissionData;
    private final ValidType validType;

    public EmissionService(EmissionRepository emissionRepository, CountryRepository countryRepository, EmissionData emissionData, ValidType validType) {
        this.emissionRepository = emissionRepository;
        this.countryRepository = countryRepository;
        this.emissionData = emissionData;
        this.validType = validType;
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

    private void checkCategoryAndTotalSums(Double solid, Double liquid, Double gasFlaring, Double gasFuel, Double cement, Double total) {
        //Check total emissions match with sum of emissions. Bunker fuels not in total
        //This does not compute well with NaN
        Double emissionSum = solid + liquid + gasFlaring + gasFuel + cement;
        Boolean totalChecks = Math.abs(emissionSum-total) <= 1;
        if (!totalChecks) {
            logger.warning("Following total and category sum didn't match: " + total +" : " + emissionSum);
        }
    }

    private String validateType(Map<String, String> allParams) {
        String emissionType = null;
        Boolean valid = false;

        if(allParams.containsKey("type")) {
            emissionType = allParams.get("type").toLowerCase(Locale.ROOT);
            if (validType.getValidTypes().containsKey(emissionType)) {
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

    private List<Integer> findDistinctYears(List<EmissionData> emissions) {
        try {
            List<Integer> years = new ArrayList<>();
            for (EmissionData emission: emissions) {
                Integer year = emission.getYear();
                if (years.contains(year)) {
                    //pass
                } else {
                    years.add(year);
                }
            }
            if (years.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No years to allocate the emissions"
                );
            } else {
                return years;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_IMPLEMENTED, "Unexpected error in separating emissions years."
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
            Double perCapita = parseEmissionsToDouble(dataRow[8]);
            Double bunkerFuels = parseEmissionsToDouble(dataRow[9]);


            //Handle name fromatting of of Viet Nam
            if(countryName.equals("VIET NAM")) {
                countryName = "VIETNAM";
            }

            checkCategoryAndTotalSums(solid, liquid, gasFlaring, gasFuel, cement, total);

            //Create and add emissions to country
            addCountry(countryName);
            Country country = countryRepository.findByName(countryName);
            Emission emission = new Emission(country, year, solid, liquid, gasFlaring, gasFuel, cement, perCapita, bunkerFuels);
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
        try {
            List<Country> polluterRows = countryRepository.findAllByOrderByName();
            List<String> polluters = new ArrayList<>();
            for (Country polluter : polluterRows) {
                String name = polluter.getName();
                polluters.add(name);
            }
            JsonNode result = turnStringListToJson(polluters);
            return result;
        } catch (ResponseStatusException e) {
             throw e;
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            throw new ResponseStatusException(
                    HttpStatus.NOT_IMPLEMENTED
            );
        }
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

    public ObjectNode createCountryNode(ObjectMapper mapper, EmissionData emission, int typeIndex) {
        try{
            ObjectNode node = mapper.createObjectNode();
            String name = emission.getCountry();
            Double pollution = emission.getType(typeIndex);
            node.put("country", name);
            node.put("pollutionAmount", pollution);
            return node;
        } catch (Exception e) {
            throw new RuntimeException("EmissionData cannot be turned to JSON node: " + e);
        }
    }

    public JsonNode turnEmissionListToJsonNode(ObjectMapper mapper, List<EmissionData> list, Integer year, int typeIndex) {
        try {
            ObjectNode polluterResultNode = mapper.createObjectNode();
            ArrayNode array = mapper.createArrayNode();
            for (EmissionData emission : list) {
                JsonNode node = createCountryNode(mapper, emission, typeIndex);
                array.add(node);
            }

            polluterResultNode.put("year", year);
            polluterResultNode.set("polluters", array);
            return polluterResultNode;
        } catch (Exception e) {
            throw new RuntimeException("Turning emissionData list to json node failed. Message: " + e);
        }
    }

    public JsonNode parseEmissionResult(List<EmissionData> list, Integer type, Integer limit) {
        try {
            if (list.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No data with these parameters."
                );
            }

            List<Integer> years = findDistinctYears(list);
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode endResult = mapper.createArrayNode();
            for (Integer year: years) {
                List<EmissionData> emissionsAtYear = list
                        .stream()
                        .filter(e -> e.getYear().equals(year))
                        .sorted(Comparator.nullsLast(
                                (e1, e2) -> e2.getType(type).compareTo(e1.getType(type))))
                        .limit(limit)
                        .collect(Collectors.toList());
                JsonNode node = turnEmissionListToJsonNode(mapper, emissionsAtYear, year, type);
                endResult.add(node);
            }
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
        Integer to = 2021;
        Integer top = 0;

        try {
            //Insert all query params
            emissionType = validateType(allParams);
            int typeIndex = validType.returnTypeIndex(emissionType);
            if (allParams.containsKey("from")) {
                from = parseYearToInt(allParams.get("from"));
            }
            if (allParams.containsKey("to")) {
                to = parseYearToInt(allParams.get("to"));
            }
            if (allParams.containsKey("top")) {
                top = Integer.parseInt(allParams.get("top"));
            }

            //TODO: if total, then other repository query
            List<Emission> polluters = emissionRepository.findAllByYearBetweenOrderByYearAsc(from, to);
            List<EmissionData> list = emissionData.transformToEmissionDataList(polluters);

            return parseEmissionResult(list, typeIndex, top);
        } catch (NumberFormatException e) {
            logger.warning("Query params were not in number format.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query params were not in number format.");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Unexpected exception: " + e);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    public ObjectNode listEndPoints() {
        String endPoint1 = "/polluters - returns all polluter countries alphabetically sorted";
        String endPoint2 = "/worst/polluters - returns the most polluting countries by year and category";

        List<String> list = new ArrayList<>();
        list.add(endPoint1);
        list.add(endPoint2);
        return turnStringListToJson(list);
    }

    public void saveEmissionToDatabase(Emission emission) {
        emissionRepository.save(emission);
    }

    public void saveCountryToDatabase(Country country) {
        countryRepository.save(country);
    }
}
