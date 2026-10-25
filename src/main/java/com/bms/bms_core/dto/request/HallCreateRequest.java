package com.bms.bms_core.dto.request;

import com.bms.bms_core.enums.SeatType;
import com.bms.bms_core.models.HallRowMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallCreateRequest {
    private Integer seatCount;
    private String hallName;
    private List<HallRowMapping> rowMappingList;

    /** Returns the seat-row mappings supplied for the hall. */
    public List<HallRowMapping> getRowMappingList() {
        return rowMappingList;
    }
}
