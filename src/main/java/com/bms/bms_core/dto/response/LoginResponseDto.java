package com.bms.bms_core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginResponseDto {
    String token;
    UserResponseDto theatreOwner;
    UserResponseDto customer;
    UserResponseDto admin;
}
