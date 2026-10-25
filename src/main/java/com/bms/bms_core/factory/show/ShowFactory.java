package com.bms.bms_core.factory.show;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.dto.request.ShowCreateRequest;
import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.utilities.CommonUtility;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ShowFactory {

    /** Builds a show entity with its owner, hall, and scheduled times. */
    public Show createShow(ShowCreateRequest showCreateRequest,
                           TheatreOwner theatreOwner,
                           Hall hall){

        Long showStartTimeInSeconds = Duration.between(Constants.WORLD_START_TIME, showCreateRequest.getShowStartTime()).getSeconds();
        Long showEndTimeInSeconds = Duration.between(Constants.WORLD_START_TIME, showCreateRequest.getShowEndTime()).getSeconds();

        Show show = Show.builder()
                .showName(showCreateRequest.getShowName())
                .showStartTime(showCreateRequest.getShowStartTime())
                .showEndTime(showCreateRequest.getShowEndTime())
                .showStartTimeInSeconds(showStartTimeInSeconds)
                .hall(hall)
                .artists(showCreateRequest.getArtists())
                .shortDescription(showCreateRequest.getShortDescription())
                .showEndTimeInSeconds(showEndTimeInSeconds)
                .build();
        CommonUtility.setGlobalRecordFields(
                show,
                Constants.SHOW_ENTITY_NAME,
                theatreOwner.getId(),
                theatreOwner.getId());
        return show;
    }
}
