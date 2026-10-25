package com.bms.bms_core.services;

import com.bms.bms_core.models.BookedSeat;
import com.bms.bms_core.enums.BookingStatus;
import com.bms.bms_core.enums.SeatStatus;
import com.bms.bms_core.repositories.BookedSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class BookedSeatService {

    private BookedSeatRepository bookedSeatRepository;

    @Autowired
    public BookedSeatService(BookedSeatRepository bookedSeatRepository){
        this.bookedSeatRepository = bookedSeatRepository;
    }

    /** Checks whether a seat label is already assigned for the show. */
    public boolean isSeatAlreadyBooked(String seatId, UUID showId){
        return bookedSeatRepository.existsBySeatIdAndShow_SysId(seatId, showId);
    }

    /** Returns whether a seat is available, pending payment, or booked. */
    @Transactional(readOnly = true)
    public SeatStatus getSeatStatus(String seatId, UUID showId) {
        return bookedSeatRepository.findBySeatIdAndShow_SysId(seatId, showId)
                .map(bookedSeat -> bookedSeat.getBooking() != null
                        && bookedSeat.getBooking().getStatus() == BookingStatus.PENDING
                        ? SeatStatus.PENDING : SeatStatus.BOOKED)
                .orElse(SeatStatus.NOT_BOOKED);
    }


}
