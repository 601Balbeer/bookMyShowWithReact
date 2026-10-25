package com.bms.bms_core.factory.user;

import com.bms.bms_core.dto.request.UserCreateRequest;
import com.bms.bms_core.enums.UserType;
import com.bms.bms_core.models.User;

/**
 * This design pattern standardise the way of creation of objects.
 */

public interface UserFactory {
    /** Retrieves user type. */
    public UserType getUserType();
    /** Builds a user entity from the account creation details. */
    public User createUser(UserCreateRequest userCreateRequest);
}
