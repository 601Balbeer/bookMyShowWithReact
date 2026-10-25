package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.LoginRequestDto;
import com.bms.bms_core.dto.response.LoginResponseDto;
import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.models.Customer;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.dto.response.UserResponseDto;
import com.bms.bms_core.models.User;
import com.bms.bms_core.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    /** Registers a customer account and returns its safe profile. */
    @PostMapping("/user/register")
    public ResponseEntity<UserResponseDto> registerTheaterOwner(
            @RequestBody UserCreateRequest userCreateRequest
            ){
        User user = userService.createUser(userCreateRequest);
        return new ResponseEntity<>(UserResponseDto.from(user), HttpStatus.CREATED);
    }

    /** Authenticates the supplied credentials and returns the login result. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequestDto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));
        LoginResponseDto loginResponseDto = userService.loginUser(authentication);
        return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
    }
}
