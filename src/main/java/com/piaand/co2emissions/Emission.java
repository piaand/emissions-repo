package com.piaand.co2emissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Data table Entity representation as Java class (row saved from csv). Every entry has an id type Long.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Emission extends AbstractPersistable<Long> {

    @NotBlank(message="Country name is mandatory")
    private String countryName;

    @Min(1751)
    @Max(2021)
    private Integer year;
}
