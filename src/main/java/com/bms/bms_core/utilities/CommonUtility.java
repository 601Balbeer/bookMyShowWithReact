package com.bms.bms_core.utilities;

import com.bms.bms_core.models.GlobalRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommonUtility {

    /** Generates a prefixed identifier for a user record. */
    public static String generateUserId(String entityName) {
        return entityName + "-" + UUID.randomUUID();
    }

    /** Sets the creator and update timestamps and usernames on a record. */
    public static synchronized void setGlobalRecordFields(GlobalRecord globalRecord,
                                                          String entityName,
                                                          String createdBy,
                                                          String updatedBy){
        globalRecord.setId(CommonUtility.generateUserId(entityName));
        globalRecord.setCreatedAt(LocalDateTime.now());
        globalRecord.setUpdatedAt(LocalDateTime.now());
        globalRecord.setCreatedBy(createdBy);
        globalRecord.setUpdatedBy(updatedBy);
    }

}
