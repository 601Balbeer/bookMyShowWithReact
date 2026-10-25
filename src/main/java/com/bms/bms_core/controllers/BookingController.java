package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.BookingCreateRequest;
import com.bms.bms_core.dto.response.BookingResponse;
import com.bms.bms_core.dto.response.BookingCancellationResponse;
import com.bms.bms_core.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Creates a pending booking and payment for the authenticated customer. */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingCreateRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, authentication.getName()));
    }

    /** Cancels the requested booking and records its refund when applicable. */
    @PostMapping("/{bookingSysId}/cancel")
    public ResponseEntity<BookingCancellationResponse> cancelBooking(
            @PathVariable UUID bookingSysId,
            Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingSysId, authentication.getName()));
    }
}
