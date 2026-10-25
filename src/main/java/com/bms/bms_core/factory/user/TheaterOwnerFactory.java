package com.bms.bms_core.factory.user;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.enums.UserType;
import com.bms.bms_core.models.TheatreOwner;
import com.bms.bms_core.utilities.CommonUtility;
import com.bms.bms_core.utilities.PasswordUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TheaterOwnerFactory implements UserFactory{

    /** Retrieves user type. */
    @Override
    public UserType getUserType() {
        return UserType.THEATER_OWNER;
    }

    /** Builds a theater-owner entity from the admin-provisioned account details. */
    public TheatreOwner createUser(UserCreateRequest userCreateRequest) {
        return TheatreOwner.builder()
                .businessName(userCreateRequest.getBusinessName())
                .panCard(userCreateRequest.getPanCard())
                .gstNumber(userCreateRequest.getGstNumber())
                .email(userCreateRequest.getEmail())
                .businessRegistrationId(userCreateRequest.getBusinessRegistrationId())
                .fullName(userCreateRequest.getFullName())
                .createdAt(LocalDateTime.now())
                .id(CommonUtility.generateUserId(Constants.THEATER_OWNER_ENTITY_NAME))
                .updatedAt(LocalDateTime.now())
                .createdBy(Constants.SYSTEM_USER_NAME)
                .updatedBy(Constants.SYSTEM_USER_NAME)
                .password(PasswordUtil.encryptPassword(userCreateRequest.getPassword()))
                .contactNumber(userCreateRequest.getContactNumber())
                .build();
    }
}
