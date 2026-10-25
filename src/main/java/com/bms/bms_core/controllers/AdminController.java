package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.dto.response.UserResponseDto;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {

        this.userService = userService;
    }

    /** Creates a theater-owner account from an admin-authorized request. */
    @PostMapping("/theater-owners")
    public ResponseEntity<UserResponseDto> createTheaterOwner(@RequestBody UserCreateRequest request) {
        TheatreOwner owner = userService.createTheaterOwner(request);
        return new ResponseEntity<>(UserResponseDto.from(owner), HttpStatus.CREATED);
    }
}
