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
@Table(name = "halls")
public class Hall extends GlobalRecord{
    private Integer seatCount;
    private String hallName;
    @ManyToOne
    private Theater theater;


}
