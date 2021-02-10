package com.piaand.co2emissions;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
* REST API controller. Collection of different end-points user may call to get emission data.
 * /polluters - returns all polluter countries alphabetically sorted
 * /worst/polluters - returns countries sorted by pollution amount in given type. Below example search:
 * "/worst/polluters?from=2000&to=2015&type=cement&top=10" - return top 10 most polluting countries in category cement grouped by year from 2000 to 2015
 * "/worst/polluters?from=2011&top=5&type=Total" - return top 5 most polluting countries overall grouped by year from 2011 onwards
 */
@RestController
public class EmissionController {
    private final EmissionService emissionService;

    public EmissionController (EmissionService emissionService) { this.emissionService = emissionService; }

    @GetMapping("/")
    public JsonNode listEndPoints() { return emissionService.listEndPoints(); }

    @GetMapping("/polluters")
    public JsonNode listAllPolluters() { return emissionService.listPollutersAlphabetically(); }

    @GetMapping("/worst/polluters")
    public JsonNode listWorstPolluters(@RequestParam Map<String,String> allParams) { return emissionService.listWorstPollutersWithFilters(allParams); }
}
