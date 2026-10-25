package com.bms.bms_core.services;

import com.bms.bms_core.dto.request.TheaterCreateRequest;
import com.bms.bms_core.exceptions.RecordNotFoundException;
import com.bms.bms_core.factory.theater.TheaterFactory;
import com.bms.bms_core.models.Location;
import com.bms.bms_core.models.Theater;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.repositories.TheaterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TheaterService {

    @Autowired
    private UserService userService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private TheaterFactory theaterFactory;
    @Autowired
    private TheaterRepository theaterRepository;

    /** Saves a theater and its location under the authenticated theater owner. */
    public Theater createTheater(TheaterCreateRequest theaterCreateRequest,
                                 String authenticatedEmail){
        TheatreOwner theatreOwner = userService.getTheaterOwnerByEmail(authenticatedEmail);
        Location location = theaterCreateRequest.getLocation();
        location = locationService.createLocation(location);
        Theater theater = theaterFactory.createTheater(
                theaterCreateRequest, location, theatreOwner
        );
        return theaterRepository.save(theater);
    }

    /** Finds a theater by its system UUID or throws when it does not exist. */
    public Theater getTheaterBySysId(UUID sysId){
        Optional<Theater> theater = theaterRepository.findById(sysId);
        if(theater.isEmpty()){
            throw new  RecordNotFoundException(
                    String.format("Theater with sysId %s does not exist in system.", sysId.toString())
            );
        }
        return theater.get();
    }

    /** Retrieves theaters by city. */
    public List<Theater> getTheatersByCity(String city){
        return theaterRepository.findByLocation_CityIgnoreCase(city);
    }

    /** Lists theaters belonging to the authenticated owner's email. */
    public List<Theater> getAllTheaterByOwnerEmail(String authenticatedEmail){
        TheatreOwner theatreOwner = userService.getTheaterOwnerByEmail(authenticatedEmail);
        return theaterRepository.findByOwner(theatreOwner);
    }



}
