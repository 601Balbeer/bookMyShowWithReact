package com.bms.bms_core.controllers;

import com.bms.bms_core.dto.request.PaymentAttemptRequest;
import com.bms.bms_core.dto.response.PaymentResponse;
import com.bms.bms_core.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Retrieves a payment attempt after checking booking ownership. */
    @GetMapping("/{paymentSysId}")
    public ResponseEntity<PaymentResponse> getPaymentAttempt(
            @PathVariable UUID paymentSysId,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.getAttempt(paymentSysId, authentication.getName()));
    }

    /** Processes the payment attempt and updates the booking when payment succeeds. */
    @PostMapping("/booking/{bookingSysId}/pay")
    public ResponseEntity<PaymentResponse> payForBooking(
            @PathVariable UUID bookingSysId,
            @RequestBody PaymentAttemptRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.payForBooking(bookingSysId, request, authentication.getName()));
    }
}
