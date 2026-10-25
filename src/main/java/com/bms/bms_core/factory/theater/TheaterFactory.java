package com.bms.bms_core.factory.theater;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.dto.request.TheaterCreateRequest;
import com.bms.bms_core.models.Location;
import com.bms.bms_core.models.Theater;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.utilities.CommonUtility;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TheaterFactory {

    /** Builds a theater entity and associates its location and owner. */
    public Theater createTheater(TheaterCreateRequest theaterCreateRequest,
                                 Location location,
                                 TheatreOwner theatreOwner){

        Theater theater = Theater.builder()
                .theaterName(theaterCreateRequest.getTheaterName())
                .owner(theatreOwner)
                .location(location)
                .build();
        CommonUtility.setGlobalRecordFields(theater,
                Constants.THEATER_ENTITY_NAME,
                theatreOwner.getId(),
                theatreOwner.getId());
        return theater;
    }

}
