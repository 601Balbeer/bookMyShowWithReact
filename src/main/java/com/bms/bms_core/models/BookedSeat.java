package com.bms.bms_core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "booked_seats", uniqueConstraints =
        @UniqueConstraint(columnNames = {"seat_id", "show_sys_id"}))
public class BookedSeat extends GlobalRecord{
    private String seatId;
    @ManyToOne
    private Show show;
    @ManyToOne
    private Booking booking;
}
