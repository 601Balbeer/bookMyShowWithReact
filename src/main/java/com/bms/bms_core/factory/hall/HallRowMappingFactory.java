package com.bms.bms_core.factory.hall;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.HallRowMapping;
import com.bms.bms_core.utilities.CommonUtility;
import org.springframework.stereotype.Component;

@Component
public class HallRowMappingFactory {

    /** Creates hall row mapping using the supplied request data. */
    public HallRowMapping createHallRowMapping(Hall hall,
                                               HallRowMapping hallRowMapping){
        HallRowMapping rowMapping = HallRowMapping.builder()
                .rowRange(hallRowMapping.getRowRange())
                .basePrice(hallRowMapping.getBasePrice())
                .hall(hall)
                .basePriceCurrency(hallRowMapping.getBasePriceCurrency())
                .seatCount(hallRowMapping.getSeatCount())
                .seatType(hallRowMapping.getSeatType())
                .build();

        CommonUtility.setGlobalRecordFields(
                rowMapping,
                Constants.HALL_ROW_MAPPING_ENTITY_NAME,
                hall.getTheater().getOwner().getId(),
                hall.getTheater().getOwner().getId()
                );

        return rowMapping;
    }

}
