package com.bms.bms_core.dto.response;

import com.bms.bms_core.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookedSeatResponse {
    private String seatId;
    private SeatType seatType;
    private Double price;
}
