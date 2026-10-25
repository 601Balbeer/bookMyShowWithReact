package com.bms.bms_core.dto.request;

import com.bms.bms_core.models.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterCreateRequest {
    private String theaterName;
    private Location location;


}
