package com.bms.bms_core.repositories;

import com.bms.bms_core.models.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, UUID> {
    /** Checks whether a seat is already assigned to a booking for the show. */
    boolean existsBySeatIdAndShow_SysId(String seatId, UUID showSysId);
    /** Finds a booked seat by its label and show ID. */
    Optional<BookedSeat> findBySeatIdAndShow_SysId(String seatId, UUID showSysId);
}
