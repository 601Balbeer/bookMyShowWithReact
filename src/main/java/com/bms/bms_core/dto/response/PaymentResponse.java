package com.bms.bms_core.dto.response;

import com.bms.bms_core.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID sysId;
    private UUID bookingSysId;
    private Double amount;
    private Double attemptedAmount;
    private LocalDateTime paymentTime;
    private String transactionId;
    private PaymentStatus paymentStatus;
}
