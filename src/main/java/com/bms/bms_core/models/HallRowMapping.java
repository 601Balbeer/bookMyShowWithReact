package com.bms.bms_core.models;

import com.bms.bms_core.enums.Currency;
import com.bms.bms_core.enums.SeatType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hall-row-mappings")
public class HallRowMapping extends GlobalRecord{
    @ManyToOne
    private Hall hall;
    @Enumerated(EnumType.STRING)
    private SeatType seatType;
    private String rowRange;
    private String seatCount;
    private Double basePrice;
    @Enumerated(EnumType.STRING)
    private Currency basePriceCurrency;


}
