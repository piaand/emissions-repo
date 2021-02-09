package com.piaand.co2emissions;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Class to create immutable valid type which contains list of emission type we know.
 */
@Component
public class ValidType {
    private final Map<String, Integer> validTypes;

    public ValidType() {
        this.validTypes = Map.of(
                "cement", 0,
                "solid", 1,
                "liquid", 2,
                "gasflaring", 3,
                "gasfuel", 4
        );
    }

    public Map<String, Integer> getValidTypes() {
        return this.validTypes;
    }

    public int returnTypeIndex(String emissionType) {
        return validTypes.get(emissionType);
    }
}
