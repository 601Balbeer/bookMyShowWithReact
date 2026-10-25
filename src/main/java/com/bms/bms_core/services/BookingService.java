package com.bms.bms_core.services;

import com.bms.bms_core.dto.request.BookingCreateRequest;
import com.bms.bms_core.dto.request.SeatTypeCountRequest;
import com.bms.bms_core.dto.response.BookedSeatResponse;
import com.bms.bms_core.dto.response.BookingResponse;
import com.bms.bms_core.dto.response.BookingCancellationResponse;
import com.bms.bms_core.dto.response.PaymentResponse;
import com.bms.bms_core.enums.BookingStatus;
import com.bms.bms_core.enums.PaymentStatus;
import com.bms.bms_core.enums.SeatType;
import com.bms.bms_core.models.BookedSeat;
import com.bms.bms_core.models.Booking;
import com.bms.bms_core.models.Customer;
import com.bms.bms_core.models.HallRowMapping;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.models.User;
import com.bms.bms_core.repositories.BookedSeatRepository;
import com.bms.bms_core.repositories.BookingRepository;
import com.bms.bms_core.repositories.HallRowMappingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final HallRowMappingRepository hallRowMappingRepository;
    private final ShowService showService;
    private final UserService userService;
    private final PaymentService paymentService;

    public BookingService(BookingRepository bookingRepository,
                          BookedSeatRepository bookedSeatRepository,
                          HallRowMappingRepository hallRowMappingRepository,
                          ShowService showService,
                          UserService userService,
                          PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.hallRowMappingRepository = hallRowMappingRepository;
        this.showService = showService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    /** Allocates requested seats and saves a pending booking with its pending payment. */
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, String email) {
        if (request == null || request.getShowSysId() == null
                || request.getSeatCounts() == null || request.getSeatCounts().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "showSysId and at least one seat type/count are required");
        }

        Map<SeatType, Integer> requestedCounts = new LinkedHashMap<>();
        for (SeatTypeCountRequest item : request.getSeatCounts()) {
            if (item == null || item.getSeatType() == null || item.getCount() == null || item.getCount() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each seatCounts item needs a seatType and positive count");
            }
            if (requestedCounts.putIfAbsent(item.getSeatType(), item.getCount()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Provide each seatType once; combine its count");
            }
        }

        User user = userService.getUserByEmail(email);
        if (!(user instanceof Customer customer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can create bookings");
        }

        Show show = showService.getShowBySysId(request.getShowSysId());
        if (show.getHall() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The selected show has no hall configured");
        }
        List<HallRowMapping> mappings = hallRowMappingRepository.findByHall(show.getHall()).stream()
                .sorted(Comparator.comparing(mapping -> mapping.getRowRange() == null ? "" : mapping.getRowRange()))
                .toList();
        List<BookedSeatResponse> allocations = allocateSeats(show, mappings, requestedCounts);

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setId("BOOK-" + UUID.randomUUID());
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setCreatedBy(email);
        booking.setUpdatedBy(email);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentAttempts(new ArrayList<>());
        Booking savedBooking = bookingRepository.save(booking);

        List<BookedSeat> bookedSeats = new ArrayList<>();
        for (BookedSeatResponse allocation : allocations) {
            BookedSeat bookedSeat = new BookedSeat();
            bookedSeat.setId("SEAT-" + UUID.randomUUID());
            bookedSeat.setCreatedAt(now);
            bookedSeat.setUpdatedAt(now);
            bookedSeat.setCreatedBy(email);
            bookedSeat.setUpdatedBy(email);
            bookedSeat.setSeatId(allocation.getSeatId());
            bookedSeat.setShow(show);
            bookedSeat.setBooking(savedBooking);
            bookedSeats.add(bookedSeatRepository.save(bookedSeat));
        }
        savedBooking.setBookedSeats(bookedSeats);

        double amount = allocations.stream().mapToDouble(BookedSeatResponse::getPrice).sum();
        PaymentResponse payment = paymentService.createPendingPayment(savedBooking, amount, email);
        return BookingResponse.builder()
                .bookingSysId(savedBooking.getSysId())
                .showSysId(show.getSysId())
                .seats(allocations)
                .amount(amount)
                .status(savedBooking.getStatus())
                .payment(payment)
                .build();
    }

    /** Allocates available seats for the requested booking. */
    private List<BookedSeatResponse> allocateSeats(Show show,
                                                   List<HallRowMapping> mappings,
                                                   Map<SeatType, Integer> requestedCounts) {
        List<BookedSeatResponse> allocated = new ArrayList<>();
        Set<String> allocatedIds = new HashSet<>();

        for (Map.Entry<SeatType, Integer> request : requestedCounts.entrySet()) {
            SeatType requestedType = request.getKey();
            int needed = request.getValue();
            int allocatedForType = 0;

            for (HallRowMapping mapping : mappings) {
                if (mapping.getSeatType() != requestedType) {
                    continue;
                }
                if (mapping.getBasePrice() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Price is not configured for seat type " + requestedType);
                }
                String[] rowBounds = mapping.getRowRange() == null ? new String[0]
                        : mapping.getRowRange().split("-", 2);
                if (rowBounds.length != 2 || rowBounds[0].trim().length() != 1
                        || rowBounds[1].trim().length() != 1) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid row range configured for seat type " + requestedType);
                }

                int seatsPerRow;
                try {
                    seatsPerRow = Integer.parseInt(mapping.getSeatCount());
                } catch (RuntimeException exception) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid seat count configured for seat type " + requestedType);
                }
                char firstRow = Character.toUpperCase(rowBounds[0].trim().charAt(0));
                char lastRow = Character.toUpperCase(rowBounds[1].trim().charAt(0));
                for (char row = firstRow; row <= lastRow && allocatedForType < needed; row++) {
                    for (int seatNumber = 1; seatNumber <= seatsPerRow && allocatedForType < needed; seatNumber++) {
                        String seatId = row + "-" + seatNumber;
                        if (allocatedIds.contains(seatId)
                                || bookedSeatRepository.existsBySeatIdAndShow_SysId(seatId, show.getSysId())) {
                            continue;
                        }
                        allocated.add(BookedSeatResponse.builder()
                                .seatId(seatId)
                                .seatType(requestedType)
                                .price(mapping.getBasePrice())
                                .build());
                        allocatedIds.add(seatId);
                        allocatedForType++;
                    }
                }
                if (allocatedForType == needed) {
                    break;
                }
            }

            if (allocatedForType < needed) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough available seats for type " + requestedType);
            }
        }
        return allocated;
    }

    /** Cancels the requested booking and records its refund when applicable. */
    @Transactional
    public BookingCancellationResponse cancelBooking(UUID bookingSysId, String email) {
        Booking booking = bookingRepository.findById(bookingSysId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (booking.getCustomer() == null || !email.equals(booking.getCustomer().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own bookings");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only confirmed bookings can be cancelled");
        }
        if (booking.getSuccessPayment() == null
                || booking.getSuccessPayment().getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking has no successful payment to cancel");
        }

        PaymentResponse paymentResponse = paymentService.refundBooking(booking, email);
        booking.setStatus(BookingStatus.CANCELLED);

        if (booking.getBookedSeats() != null && !booking.getBookedSeats().isEmpty()) {
            bookedSeatRepository.deleteAll(booking.getBookedSeats());
            booking.setBookedSeats(new ArrayList<>());
        }
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setUpdatedBy(email);
        bookingRepository.save(booking);

        return BookingCancellationResponse.builder()
                .bookingSysId(booking.getSysId())
                .bookingStatus(booking.getStatus())
                .paymentStatus(paymentResponse.getPaymentStatus())
                .message("Booking cancelled and demo refund recorded. No money was transferred.")
                .build();
    }
}
