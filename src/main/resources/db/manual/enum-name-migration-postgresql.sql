-- Run once against an existing PostgreSQL database before restarting the app
-- with EnumType.STRING mappings. Numeric values are the former Java ordinals.

ALTER TABLE "hall-row-mappings"
    ALTER COLUMN seat_type TYPE VARCHAR(255)
    USING CASE seat_type::text
        WHEN '0' THEN 'REGULAR'
        WHEN '1' THEN 'PREMIUM'
        WHEN '2' THEN 'VIP'
        WHEN '3' THEN 'RECLINER'
        WHEN '4' THEN 'SILVER'
        WHEN '5' THEN 'GOLD'
        ELSE seat_type::text
    END;

ALTER TABLE "hall-row-mappings"
    ALTER COLUMN base_price_currency TYPE VARCHAR(255)
    USING CASE base_price_currency::text
        WHEN '0' THEN 'DOLLAR'
        WHEN '1' THEN 'INR'
        WHEN '2' THEN 'EURO'
        ELSE base_price_currency::text
    END;

ALTER TABLE payment
    ALTER COLUMN payment_status TYPE VARCHAR(255)
    USING CASE payment_status::text
        WHEN '0' THEN 'PENDING'
        WHEN '1' THEN 'SUCCESS'
        WHEN '2' THEN 'FAILED'
        WHEN '3' THEN 'REFUND'
        ELSE payment_status::text
    END;

ALTER TABLE customers
    ALTER COLUMN membership_type TYPE VARCHAR(255)
    USING CASE membership_type::text
        WHEN '0' THEN 'SILVER'
        WHEN '1' THEN 'GOLD'
        WHEN '2' THEN 'PLATINUM'
        ELSE membership_type::text
    END;

ALTER TABLE bookings
    ALTER COLUMN status TYPE VARCHAR(255)
    USING CASE status::text
        WHEN '0' THEN 'PENDING'
        WHEN '1' THEN 'CONFIRMED'
        WHEN '2' THEN 'CANCELLED'
        WHEN '3' THEN 'CANCELLED'
        ELSE status::text
    END;
