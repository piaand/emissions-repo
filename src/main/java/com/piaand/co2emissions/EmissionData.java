package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

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

    interface ReturnType {
        Double getType();
    }

    private ReturnType[] returnTypes = new ReturnType[] {
            new ReturnType() { public Double getType() { return getCement(); } },
            new ReturnType() { public Double getType() { return getSolid(); } },
            new ReturnType() { public Double getType() { return getLiquid(); } },
            new ReturnType() { public Double getType() { return getGasFlaring(); } },
            new ReturnType() { public Double getType() { return getGasFuel(); } }
    };

    public Double getType(int index) {
        return returnTypes[index].getType();
    }
    //todo: add a method transforming emissions to data emissions
}
