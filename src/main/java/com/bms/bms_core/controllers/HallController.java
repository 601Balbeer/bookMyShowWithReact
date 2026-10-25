package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.HallCreateRequest;
import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.services.HallService;
import com.bms.bms_core.services.ShowService;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hall")
public class HallController {

    private static final Logger log = LoggerFactory.getLogger(HallController.class);


    private HallService hallService;
    private ShowService showService;

    @Autowired
    public HallController(HallService hallService, ShowService showService){
        this.hallService = hallService;
        this.showService = showService;
    }


    /**
     * theaterId : In which theater you want to create hall.
     * hallCreateRequest : It is containing details of the hall.
     * @param theaterId
     * @param hallCreateRequest
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Hall> registerHall(
            @RequestParam UUID theaterId,
            @RequestBody HallCreateRequest hallCreateRequest,
            Authentication authentication
            ){
        log.info("[HallController]: RegisterHall method triggered for theaterId: {}", theaterId);
        Hall hall = hallService.registerHall(theaterId, authentication.getName(), hallCreateRequest);
        return new ResponseEntity<>(hall, HttpStatus.CREATED);
    }

    /** Lists shows scheduled in the hall identified by the query parameter. */
    @GetMapping("/shows/all")
    public ResponseEntity<List<Show>> getAllShowsByHall(@RequestParam UUID hallId){
        List<Show> shows = showService.getAllShowByHallSysID(hallId);
        return new ResponseEntity<>(shows, HttpStatus.OK);
    }

}
