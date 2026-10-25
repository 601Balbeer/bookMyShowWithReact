package com.bms.bms_core.strategies.pricing;

import com.bms.bms_core.enums.SeatType;

public class WeekDayPricingStrategy implements PricingStrategy{
    /** Applies weekday pricing rules to the seat's base price. */
    @Override
    public double calculatePrice(double basePrice, SeatType seatType) {
        return switch (seatType){
            case RECLINER -> basePrice*1.5;
            case VIP ->  basePrice*2.0;
            case PREMIUM -> basePrice*3.5;
            case SILVER, GOLD -> basePrice;
            case REGULAR -> basePrice*3.0;
        };
    }
}
