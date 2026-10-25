package com.bms.bms_core.converters;

import com.bms.bms_core.enums.BookingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {
    /** Stores booking statuses using their enum names in the database. */
    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    /** Reads a database status string as a booking status enum. */
    @Override
    public BookingStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        // Accept legacy ordinal values while existing databases are migrated to names.
        return switch (value.trim().toUpperCase()) {
            case "PENDING", "0" -> BookingStatus.PENDING;
            case "CONFIRMED", "1" -> BookingStatus.CONFIRMED;
            case "CANCELLED", "2", "3" -> BookingStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown persisted booking status: " + value);
        };
    }
}
