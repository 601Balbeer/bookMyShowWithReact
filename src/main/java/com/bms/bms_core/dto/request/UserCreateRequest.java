package com.bms.bms_core.dto.request;

import com.bms.bms_core.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String fullName;
    private String email;
    private String password;
    private String contactNumber;
    private LocalDate dateOfBirth;
    private String businessName;
    private String panCard;
    private String businessRegistrationId;
    private String gstNumber;
    private UserType userType;
}
