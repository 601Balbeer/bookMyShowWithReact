package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.response.SeatStatusResponse;
import com.bms.bms_core.dto.request.ShowCreateRequest;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.services.ShowService;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/show")
public class ShowController {

    private ShowService showService;

    @Autowired
    public ShowController(ShowService showService){
        this.showService = showService;
    }

    /** Creates a show in the specified hall for the authenticated theater owner. */
    @PostMapping("/create")
    public ResponseEntity<Show> createShow(
            @RequestParam UUID hallId,
            @RequestBody ShowCreateRequest showCreateRequest,
            Authentication authentication
            ){
        Show show = showService.createShow(hallId, showCreateRequest, authentication.getName());
        return new ResponseEntity<>(show, HttpStatus.CREATED);
    }

    /**
     * This endpoint will get us the seat status of all the seats in a show.
     */
    @GetMapping("/seat-status/{showSysId}")
    public ResponseEntity<List<SeatStatusResponse>> getSeatStatusByShowSysId(
            @PathVariable UUID showSysId
    ){
        List<SeatStatusResponse> seatStatusResponses = showService.getAllSeatStatusByShowSysId(showSysId);
        return new ResponseEntity<>(seatStatusResponses, HttpStatus.OK);
    }

    /** Lists shows scheduled in the specified theater. */
    @GetMapping("/get/{theatreSysId}")
    public ResponseEntity<List<Show>> getShowsByTheatreSysId(@PathVariable UUID theatreSysId){
        List<Show> shows = showService.getAllShowsByTheatre(theatreSysId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }
}
