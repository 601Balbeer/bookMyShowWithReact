package com.bms.bms_core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theatre-owners")
public class TheatreOwner extends User {
    private String businessName;
    @JsonIgnore
    private String panCard;
    @JsonIgnore
    private String businessRegistrationId;
    @JsonIgnore
    private String gstNumber;
}
