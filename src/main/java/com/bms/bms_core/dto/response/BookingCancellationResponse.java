package com.bms.bms_core.dto.response;

import com.bms.bms_core.enums.BookingStatus;
import com.bms.bms_core.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancellationResponse {
    private UUID bookingSysId;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private String message;
}
