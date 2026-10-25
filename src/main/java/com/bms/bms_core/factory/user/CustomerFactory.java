package com.bms.bms_core.factory.user;

import com.bms.bms_core.config.Constants;
import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.enums.UserType;
import com.bms.bms_core.models.Customer;
import com.bms.bms_core.utilities.CommonUtility;
import com.bms.bms_core.utilities.PasswordUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomerFactory implements UserFactory{
    /** Retrieves user type. */
    @Override
    public UserType getUserType() {
        return UserType.CUSTOMER;
    }

    /** Builds a customer entity from the registration details and hashes its password. */
    @Override
    public Customer createUser(UserCreateRequest userCreateRequest) {
        return Customer.builder()
                .fullName(userCreateRequest.getFullName())
                .createdAt(LocalDateTime.now())
                .email(userCreateRequest.getEmail())
                .dateOfBirth(userCreateRequest.getDateOfBirth())
                .id(CommonUtility.generateUserId(Constants.CUSTOMER_ENTITY_NAME))
                .updatedAt(LocalDateTime.now())
                .createdBy(Constants.SYSTEM_USER_NAME)
                .updatedBy(Constants.SYSTEM_USER_NAME)
                .password(PasswordUtil.encryptPassword(userCreateRequest.getPassword()))
                .contactNumber(userCreateRequest.getContactNumber())
                .build();
    }
}
