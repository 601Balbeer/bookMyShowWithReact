package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.TheaterCreateRequest;
import com.bms.bms_core.models.Theater;
import com.bms.bms_core.services.TheaterService;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/theatre")
public class TheatreController {

    @Autowired
    private TheaterService theaterService;
    /** Creates a theater for the owner authenticated by the JWT. */
    @PostMapping("/create")
    public ResponseEntity<Theater> createTheater(
            @RequestBody TheaterCreateRequest theaterCreateRequest,
            Authentication authentication
            ){
        Theater theater = theaterService.createTheater(theaterCreateRequest, authentication.getName());
        return new ResponseEntity<>(theater, HttpStatus.CREATED);
    }


    /** Retrieves theater by city. */
    @GetMapping("/get")
    public ResponseEntity<List<Theater>> getTheaterByCity(@RequestParam String city){
        List<Theater> theaters = theaterService.getTheatersByCity(city);
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }

    /** Retrieves all theater by owner. */
    @GetMapping("/get/all")
    public ResponseEntity<List<Theater>> getAllTheaterByOwner(Authentication authentication) {
        List<Theater> theaters = theaterService.getAllTheaterByOwnerEmail(authentication.getName());
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }


}
