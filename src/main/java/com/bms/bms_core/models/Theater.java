package com.bms.bms_core.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theaters")
public class Theater extends GlobalRecord {
    private String theaterName;
    @OneToOne
    private Location location;
    @ManyToOne
    private TheatreOwner owner;
}
