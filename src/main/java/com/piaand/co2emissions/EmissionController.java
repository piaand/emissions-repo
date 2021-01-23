package com.piaand.co2emissions;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* REST API controller. Collection of different end-points user may call to get emission data.
 * /polluters - returns all polluter countries alphabetically sorted
 */
@RestController
public class EmissionController {
    private final EmissionService emissionService;

    public EmissionController (EmissionService emissionService) { this.emissionService = emissionService; }

    @GetMapping("/")
    public JsonNode listEndPoints() { return emissionService.listEndPoints(); }

    @GetMapping("/polluters")
    public JsonNode listAllPolluters() { return emissionService.listPollutersAlphabetically(); }
}
