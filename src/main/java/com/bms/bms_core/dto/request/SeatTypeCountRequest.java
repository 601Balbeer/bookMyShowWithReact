package com.bms.bms_core.dto.request;

import com.bms.bms_core.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatTypeCountRequest {
    private SeatType seatType;
    private Integer count;
}
