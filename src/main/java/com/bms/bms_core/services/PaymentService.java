package com.bms.bms_core.services;

import com.bms.bms_core.dto.response.PaymentResponse;
import com.bms.bms_core.dto.request.PaymentAttemptRequest;
import com.bms.bms_core.enums.PaymentStatus;
import com.bms.bms_core.enums.BookingStatus;
import com.bms.bms_core.models.BookedSeat;
import com.bms.bms_core.models.Booking;
import com.bms.bms_core.models.HallRowMapping;
import com.bms.bms_core.models.Payment;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.repositories.BookingRepository;
import com.bms.bms_core.repositories.HallRowMappingRepository;
import com.bms.bms_core.repositories.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final HallRowMappingRepository hallRowMappingRepository;

    public PaymentService(BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          HallRowMappingRepository hallRowMappingRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.hallRowMappingRepository = hallRowMappingRepository;
    }

    /** Creates and saves a pending payment for the booking's calculated total. */
    @Transactional
    public PaymentResponse createPendingPayment(Booking booking, double amount, String authenticatedEmail) {
        if (booking.getBookedSeats() == null || booking.getBookedSeats().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking has no seats to pay for");
        }
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();
        payment.setId("PAY-" + UUID.randomUUID());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setCreatedBy(authenticatedEmail);
        payment.setUpdatedBy(authenticatedEmail);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setBooking(booking);

        Payment savedPayment = paymentRepository.save(payment);
        booking.setPaymentAttempts(new ArrayList<>(List.of(savedPayment)));
        bookingRepository.save(booking);

        return toResponse(savedPayment);
    }

    /** Processes the payment attempt and updates the booking when payment succeeds. */
    @Transactional
    public PaymentResponse payForBooking(UUID bookingSysId, PaymentAttemptRequest request, String authenticatedEmail) {
        Booking booking = bookingRepository.findById(bookingSysId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        requireBookingOwner(booking, authenticatedEmail);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is not awaiting payment");
        }
        List<Payment> attempts = booking.getPaymentAttempts() == null
                ? new ArrayList<>() : new ArrayList<>(booking.getPaymentAttempts());
        Payment payment = attempts.stream()
                .filter(attempt -> attempt.getPaymentStatus() == PaymentStatus.PENDING)
                .findFirst().orElse(null);
        if (payment == null) {
            Payment mostRecent = attempts.stream()
                    .max(java.util.Comparator.comparing(Payment::getCreatedAt,
                            java.util.Comparator.nullsFirst(
                                    java.util.Comparator.<LocalDateTime>naturalOrder())))
                    .orElse(null);
            if (mostRecent != null && mostRecent.getPaymentStatus() == PaymentStatus.FAILED) {
                LocalDateTime createdAt = LocalDateTime.now();
                payment = new Payment();
                payment.setId("PAY-" + UUID.randomUUID());
                payment.setCreatedAt(createdAt);
                payment.setUpdatedAt(createdAt);
                payment.setCreatedBy(authenticatedEmail);
                payment.setUpdatedBy(authenticatedEmail);
                payment.setAmount(mostRecent.getAmount());
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setBooking(booking);
                payment = paymentRepository.save(payment);
                attempts.add(payment);
                booking.setPaymentAttempts(attempts);
            }
        }
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No pending payment found for booking");
        }

        if (request == null || request.getAmount() == null || request.getAmount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-negative payment amount is required");
        }

        LocalDateTime now = LocalDateTime.now();
        payment.setAttemptedAmount(request.getAmount());
        payment.setPaymentTime(now);
        payment.setUpdatedAt(now);
        payment.setUpdatedBy(authenticatedEmail);
        if (BigDecimal.valueOf(request.getAmount()).compareTo(BigDecimal.valueOf(payment.getAmount())) != 0) {
            // A mismatched amount is recorded as failed; no real payment is attempted.
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setTransactionId("DEMO-FAILED-" + UUID.randomUUID());
            return toResponse(paymentRepository.save(payment));
        }

        // Local demo only: an exact amount simulates a successful payment; no gateway is contacted.
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("DEMO-PAYMENT-" + UUID.randomUUID());
        Payment savedPayment = paymentRepository.save(payment);

        booking.setSuccessPayment(savedPayment);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(now);
        booking.setUpdatedBy(authenticatedEmail);
        bookingRepository.save(booking);
        return toResponse(savedPayment);
    }

    /** Records a refund for the successfully paid booking. */
    @Transactional
    public PaymentResponse refundBooking(Booking booking, String authenticatedEmail) {
        Payment payment = booking.getSuccessPayment();
        if (payment == null || payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a successfully paid booking can be refunded");
        }
        LocalDateTime now = LocalDateTime.now();
        payment.setPaymentStatus(PaymentStatus.REFUND);
        payment.setPaymentTime(now);
        payment.setTransactionId("DEMO-REFUND-" + UUID.randomUUID());
        payment.setUpdatedAt(now);
        payment.setUpdatedBy(authenticatedEmail);
        return toResponse(paymentRepository.save(payment));
    }

    /** Retrieves a payment attempt after verifying that the requester owns its booking. */
    @Transactional(readOnly = true)
    public PaymentResponse getAttempt(UUID paymentSysId, String authenticatedEmail) {
        Payment payment = paymentRepository.findById(paymentSysId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment attempt not found"));
        if (payment.getBooking() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment attempt not found");
        }
        requireBookingOwner(payment.getBooking(), authenticatedEmail);
        return toResponse(payment);
    }

    /** Validates the required booking owner and rejects unauthorized access. */
    private void requireBookingOwner(Booking booking, String authenticatedEmail) {
        if (booking.getCustomer() == null || booking.getCustomer().getEmail() == null
                || !booking.getCustomer().getEmail().equals(authenticatedEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own payments");
        }
    }

    /** Adds the configured prices for all seats in the booking. */
    private double calculateBookingAmount(List<BookedSeat> bookedSeats) {
        Map<UUID, List<HallRowMapping>> mappingsByHallId = new HashMap<>();
        double total = 0;

        for (BookedSeat bookedSeat : bookedSeats) {
            Show show = bookedSeat.getShow();
            if (show == null || show.getHall() == null || bookedSeat.getSeatId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking contains an invalid seat");
            }

            UUID hallId = show.getHall().getSysId();
            List<HallRowMapping> mappings = mappingsByHallId.computeIfAbsent(
                    hallId, ignored -> hallRowMappingRepository.findByHall(show.getHall()));
            total += findSeatPrice(bookedSeat.getSeatId(), mappings);
        }

        return total;
    }

    /** Finds the configured price for a seat using its row and seat number. */
    private double findSeatPrice(String seatId, List<HallRowMapping> mappings) {
        String[] seatParts = seatId.split("-", 2);
        if (seatParts.length != 2 || seatParts[0].isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat id: " + seatId);
        }
        char row = Character.toUpperCase(seatParts[0].trim().charAt(0));
        int seatNumber;
        try {
            seatNumber = Integer.parseInt(seatParts[1].trim());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat id: " + seatId);
        }
        if (seatNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat id: " + seatId);
        }

        for (HallRowMapping mapping : mappings) {
            String[] rowBounds = mapping.getRowRange() == null ? new String[0]
                    : mapping.getRowRange().split("-", 2);
            if (rowBounds.length == 2 && rowBounds[0].trim().length() == 1
                    && rowBounds[1].trim().length() == 1
                    && row >= Character.toUpperCase(rowBounds[0].trim().charAt(0))
                    && row <= Character.toUpperCase(rowBounds[1].trim().charAt(0))) {
                if (mapping.getSeatCount() == null
                        || seatNumber > Integer.parseInt(mapping.getSeatCount())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Seat does not exist: " + seatId);
                }
                if (mapping.getBasePrice() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat price is not configured");
                }
                return mapping.getBasePrice();
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No price configured for seat " + seatId);
    }

    /** Maps a payment entity to the API response DTO. */
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .sysId(payment.getSysId())
                .bookingSysId(payment.getBooking().getSysId())
                .amount(payment.getAmount())
                .attemptedAmount(payment.getAttemptedAmount())
                .paymentTime(payment.getPaymentTime())
                .transactionId(payment.getTransactionId())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
