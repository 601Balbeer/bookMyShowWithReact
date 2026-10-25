package com.bms.bms_core.dto.response;

import com.bms.bms_core.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private UUID bookingSysId;
    private UUID showSysId;
    private List<BookedSeatResponse> seats;
    private Double amount;
    private BookingStatus status;
    private PaymentResponse payment;
}
