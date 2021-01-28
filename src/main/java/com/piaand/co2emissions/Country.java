package com.piaand.co2emissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CodePointLength;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Data table representation as Java class - unique polluter countries. Every entry has an id type Long.
 */

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Country extends AbstractPersistable<Long> {

    @NotBlank
    @Column(unique = true)
    private String name;

    @JsonIgnoreProperties("country")
    @OneToMany(mappedBy = "country",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Emission> emissions = new ArrayList<>();
}
