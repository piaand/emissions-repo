package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data wrapper that handles and transforms the data from db.
 * Interface Return type is closest to the function pointer array I think of in Java.
 *  - calling method getType with index number returns the respective getter for the type
 *      * i.e getCement() with 0 or getSolid() with 1
 *
 */
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmissionData {
    private String country;
    private Integer year;
    private Double solid;
    private Double liquid;
    private Double gasFlaring;
    private Double gasFuel;
    private Double cement;
    private Double perCapita;
    private Double bunkerFuels;

    interface ReturnType {
        Double getType();
    }

    //The order in array is tied to indexes in class ValidType
    private ReturnType[] returnTypes = new ReturnType[] {
            new ReturnType() { public Double getType() { return getCement(); } },
            new ReturnType() { public Double getType() { return getSolid(); } },
            new ReturnType() { public Double getType() { return getLiquid(); } },
            new ReturnType() { public Double getType() { return getGasFlaring(); } },
            new ReturnType() { public Double getType() { return getGasFuel(); } },
            new ReturnType() { public Double getType() { return getPerCapita(); } },
            new ReturnType() { public Double getType() { return getBunkerFuels(); } }
    };

    public Double getType(int index) {
        return returnTypes[index].getType();
    }

    public EmissionData transformToEmissionData(Emission emission) {
        try {
            EmissionData data = new EmissionData();
            data.setCement(emission.getCement());
            data.setCountry(emission.getCountry().getName());
            data.setGasFlaring(emission.getGasFlaring());
            data.setLiquid(emission.getLiquid());
            data.setGasFuel(emission.getGasFuel());
            data.setSolid(emission.getSolid());
            data.setYear(emission.getYear());
            data.setPerCapita(emission.getPerCapita());
            data.setBunkerFuels(emission.getBunkerFuels());

            return data;
        } catch (Exception e) {
            throw new RuntimeException("Emission " + emission.getId() + " cannot be transformed to emissionData. " + e);
        }
    }

    public List<EmissionData> transformToEmissionDataList(List<Emission> emissions) {
        try {
            List<EmissionData> list = emissions.stream().map(emission -> transformToEmissionData(emission)).collect(Collectors.toList());
            return (list);
        } catch (Exception e) {
            throw new RuntimeException("Transforming Emission list failed. Message: " + e);
        }
    }

}
