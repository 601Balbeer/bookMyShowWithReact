package com.bms.bms_core.strategies.pricing;

import com.bms.bms_core.enums.SeatType;

public interface PricingStrategy {
    /** Calculates a seat price from its base price and seat category. */
    public double calculatePrice(double basePrice, SeatType seatType);
}
