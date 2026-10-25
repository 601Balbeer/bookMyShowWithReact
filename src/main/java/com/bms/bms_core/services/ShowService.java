package com.bms.bms_core.services;

import com.bms.bms_core.dto.response.SeatStatusResponse;
import com.bms.bms_core.dto.request.ShowCreateRequest;
import com.bms.bms_core.enums.SeatStatus;
import com.bms.bms_core.exceptions.OverlappingTimeException;
import com.bms.bms_core.exceptions.RecordNotFoundException;
import com.bms.bms_core.factory.show.ShowFactory;
import com.bms.bms_core.models.*;
import com.bms.bms_core.repositories.ShowRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
@Slf4j
public class ShowService {


    private TheaterService theaterService;
    private HallService hallService;
    private UserService userService;
    private ShowFactory showFactory;
    private ShowRepository showRepository;
    private BookedSeatService bookedSeatService;

    @Autowired
    public ShowService(TheaterService theaterService,
                       HallService hallService,
                       ShowFactory showFactory,
                       ShowRepository showRepository,
                       UserService userService,
                       BookedSeatService bookedSeatService){
        this.theaterService = theaterService;
        this.userService = userService;
        this.showRepository = showRepository;
        this.hallService = hallService;
        this.showFactory = showFactory;
        this.bookedSeatService = bookedSeatService;
    }

    /** Creates a show in an owner's hall after validating ownership and schedule conflicts. */
    @Transactional
    public Show createShow(
            UUID hallSysId,
            ShowCreateRequest showCreateRequest,
            String authenticatedEmail
            ){

        TheatreOwner theatreOwner = userService.getTheaterOwnerByEmail(authenticatedEmail);
        Hall hall = hallService.getHallBySysId(hallSysId);
        if (hall.getTheater() == null || hall.getTheater().getOwner() == null
                || !theatreOwner.getSysId().equals(hall.getTheater().getOwner().getSysId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create shows in halls belonging to your own theaters");
        }
        Show show = showFactory.createShow(showCreateRequest, theatreOwner, hall);

        if(isShowOverLapping(show)){
            throw new OverlappingTimeException("Show with overlapping timings already present");
        }

        return this.saveShow(show);
    }

    /** Checks whether the new show overlaps any show already scheduled in its hall. */
    public boolean isShowOverLapping(Show show){
        List<Show> alreadyCreatedShows = showRepository.findByHall(show.getHall());
        Collections.sort(alreadyCreatedShows, (a, b) -> Math.toIntExact(a.getShowStartTimeInSeconds() - b.getShowStartTimeInSeconds()));

        for(Show s : alreadyCreatedShows){
            if(s.getShowEndTimeInSeconds() >= show.getShowStartTimeInSeconds()){
                return true;
            }
        }

        return false;
    }

    /** Finds a show by its system UUID or throws when it does not exist. */
    public Show getShowBySysId(UUID showSysId){
        Optional<Show> show = showRepository.findById(showSysId);
        if(show.isEmpty()){
            throw new RecordNotFoundException(
                    String.format("Show with sysId %s does not exist in system", showSysId.toString())
            );
        }
        return show.get();
    }

    /** Lists all shows scheduled in the hall identified by its UUID. */
    public List<Show> getAllShowByHallSysID(UUID hallSysId){
        Hall hall = hallService.getHallBySysId(hallSysId);
        return showRepository.findByHall(hall);
    }

    /** Builds availability details for every configured seat in the show. */
    public List<SeatStatusResponse> getAllSeatStatusByShowSysId(
           UUID showSysId
    ){
        Show show = this.getShowBySysId(showSysId);
        Hall hall = show.getHall();
        // HallRowMapping-1 -> A-D, 10 -> 40
        // HallRowMapping-2 -> E-F, 5 -> 10
        List<HallRowMapping> hallRowMappings = hallService.getAllRowMappingByHall(hall);
        List<SeatStatusResponse> seatStatusResponses = new ArrayList<>();
        for(HallRowMapping hallRowMapping : hallRowMappings){
            String rowRange = hallRowMapping.getRowRange();
            int seatCountPerRow = Integer.parseInt(hallRowMapping.getSeatCount());
            char stRow = rowRange.charAt(0);
            char enRow = rowRange.charAt(2);
            for(char i = stRow; i <= enRow; i++){
                for(int j = 1; j <= seatCountPerRow; j++){
                    String seatId = i + "-" + j;
                    SeatStatusResponse seatStatusResponse = SeatStatusResponse.builder()
                            .seatStatus(bookedSeatService.getSeatStatus(seatId, showSysId))
                            .seatNumber(seatId)
                            .hallId(hall.getId())
                            .theaterId(hall.getTheater().getId())
                            .showId(show.getId())
                            .seatType(hallRowMapping.getSeatType())
                            .build();
                    seatStatusResponses.add(seatStatusResponse);
                }
            }
        }

        return seatStatusResponses;
    }


    /** Lists all shows scheduled in the specified theater. */
    public List<Show> getAllShowsByTheatre(UUID theatreSysId){
        Theater theater = theaterService.getTheaterBySysId(theatreSysId);
        return showRepository.findByHall_Theater(theater);
    }

    /** Saves show to the repository. */
    public Show saveShow(Show show){
        return showRepository.save(show);
    }


}
