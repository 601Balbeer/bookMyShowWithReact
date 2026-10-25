package com.bms.bms_core.services;

import com.bms.bms_core.dto.response.LoginResponseDto;
import com.bms.bms_core.dto.response.UserResponseDto;
import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.enums.UserType;
import com.bms.bms_core.exceptions.UserNotFoundException;
import com.bms.bms_core.factory.user.CustomerFactory;
import com.bms.bms_core.factory.user.TheaterOwnerFactory;
import com.bms.bms_core.models.Customer;
import com.bms.bms_core.models.Admin;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.models.User;
import com.bms.bms_core.repositories.CustomerRepository;
import com.bms.bms_core.repositories.AdminRepository;
import com.bms.bms_core.repositories.TheaterOwnerRepository;
import com.bms.bms_core.repositories.UserRepository;
import com.bms.bms_core.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Autowired
    CustomerFactory customerFactory;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    TheaterOwnerRepository theaterOwnerRepository;
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    TheaterOwnerFactory theaterOwnerFactory;
    @Autowired
    JwtService jwtService;

    /** Registers a customer account; public registration cannot create elevated roles. */
    public User createUser(UserCreateRequest userCreateRequest){
        if (userCreateRequest.getUserType() != UserType.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Public registration is only available for customers. Theater-owner accounts require trusted approval.");
        }
        Customer customer = customerFactory.createUser(userCreateRequest);
        return customerRepository.save(customer);
    }

    /** Provisions a theater-owner account for an authorized admin request. */
    public TheatreOwner createTheaterOwner(UserCreateRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        return theaterOwnerRepository.save(theaterOwnerFactory.createUser(request));
    }

    /** Retrieves user by email. */
    public User getUserByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    /** Retrieves theater owner by email. */
    public TheatreOwner getTheaterOwnerByEmail(String email) {
        User user = getUserByEmail(email);
        if (!(user instanceof TheatreOwner theatreOwner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A theater-owner account is required");
        }
        return theatreOwner;
    }


    /** Authenticates the supplied credentials and returns the login result. */
    public LoginResponseDto loginUser(Authentication authentication){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
           return new UserNotFoundException(String.format("User with email %s does not exist", email));
        });

        TheatreOwner theatreOwner = theaterOwnerRepository.findById(user.getSysId()).orElse(null);
        Customer customer = customerRepository.findById(user.getSysId()).orElse(null);
        Admin admin = adminRepository.findById(user.getSysId()).orElse(null);
        return LoginResponseDto.builder()
                .theatreOwner(theatreOwner == null ? null : UserResponseDto.from(theatreOwner))
                .customer(customer == null ? null : UserResponseDto.from(customer))
                .admin(admin == null ? null : UserResponseDto.from(admin))
                .token(jwtService.generateToken(authentication))
                .build();


    }


    /** Finds a customer by system UUID or throws when the account does not exist. */
    public Customer getCustomerBySysId(UUID sysId){
        Optional<Customer> customer = customerRepository.findById(sysId);
        if(customer.isEmpty()){
            log.error("[UserService]:Theater owner with sysId {} does not exist in system", sysId.toString());
            throw new UserNotFoundException(
                    String.format("customer with sysId %s does not exist in system", sysId.toString())
            );
        }
        return customer.get();
    }


    /** Finds a theater owner by system UUID or throws when the account does not exist. */
    public TheatreOwner getTheaterOwnerBySysId(UUID sysId){
        Optional<TheatreOwner> theatreOwner = theaterOwnerRepository.findById(sysId);
        if(theatreOwner.isEmpty()){
            log.error("[UserService]:Theater owner with sysId {} does not exist in system", sysId.toString());
            throw new UserNotFoundException(
                    String.format("Theater owner with sysId %s does not exist in system", sysId.toString())
            );
        }
        return theatreOwner.get();
    }




}
