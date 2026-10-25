package com.bms.bms_core.factory.hall;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.dto.request.HallCreateRequest;
import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.Theater;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.utilities.CommonUtility;
import org.springframework.stereotype.Component;

@Component
public class HallFactory {
    /** Builds a hall entity associated with the theater and its owner. */
    public Hall createHall(Theater theater,
                           HallCreateRequest hallCreateRequest,
                           TheatreOwner theatreOwner){
        Hall hall = Hall.builder()
                .hallName(hallCreateRequest.getHallName())
                .seatCount(hallCreateRequest.getSeatCount())
                .theater(theater)
                .build();
        CommonUtility.setGlobalRecordFields(
                hall,
                Constants.HALL_ENTITY_NAME,
                theatreOwner.getId(),
                theatreOwner.getId());
        return hall;

    }
}
