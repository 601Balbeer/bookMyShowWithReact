package com.bms.bms_core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "shows")
public class Show extends GlobalRecord{
    private String showName;
    @ManyToOne
    private Hall hall;
    private String shortDescription;
    private String artists;
    private LocalDateTime showStartTime;
    private LocalDateTime showEndTime;
    private Long showStartTimeInSeconds;
    private Long showEndTimeInSeconds;




}
