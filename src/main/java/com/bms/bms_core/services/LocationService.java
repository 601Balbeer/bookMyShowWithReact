package com.bms.bms_core.services;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.models.Location;
import com.bms.bms_core.repositories.LocationRepository;
import com.bms.bms_core.utilities.CommonUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LocationService {

    @Autowired
    LocationRepository locationRepository;

    /** Adds audit fields and saves the location record. */
    public Location createLocation(Location location){
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        location.setCreatedBy(Constants.SYSTEM_USER_NAME);
        location.setUpdatedBy(Constants.SYSTEM_USER_NAME);
        location.setId(CommonUtility.generateUserId(Constants.LOCATION_ENTITY_NAME));
        return locationRepository.save(location);
    }
}
