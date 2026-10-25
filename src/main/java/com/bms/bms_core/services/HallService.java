package com.bms.bms_core.services;

import com.bms.bms_core.dto.request.HallCreateRequest;
import com.bms.bms_core.exceptions.RecordNotFoundException;
import com.bms.bms_core.factory.hall.HallFactory;
import com.bms.bms_core.factory.hall.HallRowMappingFactory;
import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.HallRowMapping;
import com.bms.bms_core.models.Theater;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.repositories.HallRepository;
import com.bms.bms_core.repositories.HallRowMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class HallService {

    private UserService userService;
    private TheaterService theaterService;
    private HallFactory hallFactory;
    private HallRepository hallRepository;
    private HallRowMappingFactory hallRowMappingFactory;
    private HallRowMappingRepository hallRowMappingRepository;


    @Autowired
    public HallService(UserService userService,
                       TheaterService theaterService,
                       HallFactory hallFactory,
                       HallRepository hallRepository,
                       HallRowMappingFactory hallRowMappingFactory,
                       HallRowMappingRepository hallRowMappingRepository){
        this.theaterService = theaterService;
        this.userService  = userService;
        this.hallFactory = hallFactory;
        this.hallRepository = hallRepository;
        this.hallRowMappingFactory = hallRowMappingFactory;
        this.hallRowMappingRepository = hallRowMappingRepository;

    }


    /** Creates a hall and its row mappings after checking theater ownership. */
    @Transactional
    public Hall registerHall(UUID theaterId,
                             String authenticatedEmail,
                             HallCreateRequest hallCreateRequest){
        TheatreOwner theatreOwner = userService.getTheaterOwnerByEmail(authenticatedEmail);
        Theater theater = theaterService.getTheaterBySysId(theaterId);
        if (theater.getOwner() == null
                || !theatreOwner.getSysId().equals(theater.getOwner().getSysId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only add halls to your own theaters");
        }
        Hall hall = hallFactory.createHall(theater, hallCreateRequest, theatreOwner);
        hall = this.saveHall(hall);

        for(HallRowMapping rowMapping : hallCreateRequest.getRowMappingList()){
            rowMapping = hallRowMappingFactory.createHallRowMapping(hall, rowMapping);
            this.saveHallRowMapping(rowMapping);
        }

        return hall;
    }

    /** Saves hall row mapping to the repository. */
    public HallRowMapping saveHallRowMapping(HallRowMapping hallRowMapping){
        return this.hallRowMappingRepository.save(hallRowMapping);
    }

    /** Saves all row mappings to the repository. */
    public void saveAllRowMappings(List<HallRowMapping> rowMappings){
        this.hallRowMappingRepository.saveAll(rowMappings);
    }

    /** Saves hall to the repository. */
    public Hall saveHall(Hall hall){
        return this.hallRepository.save(hall);
    }

    /** Finds a hall by its system UUID or throws when it does not exist. */
    public Hall getHallBySysId(UUID sysId){
        Optional<Hall> hall = hallRepository.findById(sysId);
        if(hall.isEmpty()){
            throw new RecordNotFoundException(
                    String.format("Hall with sysId %s does not exist in system.", sysId.toString())
            );
        }
        return hall.get();
    }

    /** Lists seat-row configurations registered for the hall. */
    public List<HallRowMapping> getAllRowMappingByHall(Hall hall){
        return hallRowMappingRepository.findByHall(hall);
    }


}
