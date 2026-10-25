package com.bms.bms_core.dto.response;

import com.bms.bms_core.models.Customer;
import com.bms.bms_core.models.Admin;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.models.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private UUID sysId;
    private String id;
    private String fullName;
    private String email;
    private String contactNumber;
    private String userType;
    private LocalDate dateOfBirth;
    private String businessName;

    /** Maps a user entity to a response that omits sensitive account fields. */
    public static UserResponseDto from(User user) {
        UserResponseDto.UserResponseDtoBuilder builder = UserResponseDto.builder()
                .sysId(user.getSysId())
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber());

        if (user instanceof Customer customer) {
            builder.userType("CUSTOMER").dateOfBirth(customer.getDateOfBirth());
        } else if (user instanceof TheatreOwner owner) {
            builder.userType("THEATER_OWNER").businessName(owner.getBusinessName());
        } else if (user instanceof Admin) {
            builder.userType("ADMIN");
        }
        return builder.build();
    }
}
