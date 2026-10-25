package com.bms.bms_core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequest {
    private UUID showSysId;
    private List<SeatTypeCountRequest> seatCounts;
}
