package com.bms.bms_core.models;

import com.bms.bms_core.enums.BookingStatus;
import com.bms.bms_core.converters.BookingStatusConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking extends GlobalRecord{
    @OneToMany(mappedBy = "booking")
    private List<BookedSeat> bookedSeats;
    @ManyToOne
    private Customer customer;
    @OneToMany(mappedBy = "booking")
    private List<Payment> paymentAttempts;
    @OneToOne
    private Payment successPayment;
    @Convert(converter = BookingStatusConverter.class)
    private BookingStatus status;
}
