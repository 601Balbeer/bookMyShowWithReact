package com.bms.bms_core.config;

import java.time.LocalDateTime;

public class Constants {
    public static final String THEATER_OWNER_ENTITY_NAME = "THEATER-OWNER";
    public static final String CUSTOMER_ENTITY_NAME = "CUSTOMER";
    public static final String SYSTEM_USER_NAME = "system";
    public static final String LOCATION_ENTITY_NAME = "LOCATION";
    public static final String THEATER_ENTITY_NAME = "THEATER";
    public static final String HALL_ENTITY_NAME = "HALL";
    public static final String HALL_ROW_MAPPING_ENTITY_NAME = "HALL-ROW-MAPPING";
    public static final LocalDateTime WORLD_START_TIME = LocalDateTime.of(2020, 1,1, 0, 0, 0 );
    public static final String SHOW_ENTITY_NAME = "SHOW";
    public static final Integer PASSWORD_SALT_LENGTH = 12;
}
