package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Data table Entity representation as Java class (row saved from csv). Every entry has an id type Long.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Emission extends AbstractPersistable<Long> {

    @ManyToOne
    private Country country;

    @Min(1751)
    @Max(2021)
    private Integer year;

    @DecimalMin("0.0")
    private Double solid;
    @DecimalMin("0.0")
    private Double liquid;
    @DecimalMin("0.0")
    private Double gasFlaring;
    @DecimalMin("0.0")
    private Double gasFuel;
    @DecimalMin("0.0")
    private Double cement;
}
