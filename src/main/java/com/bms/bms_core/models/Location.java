package com.bms.bms_core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "locations")
public class Location extends GlobalRecord{
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String country;
    private String state;
    private int lat;
    private int log;
    private String city;
    private int zipCode;
}
