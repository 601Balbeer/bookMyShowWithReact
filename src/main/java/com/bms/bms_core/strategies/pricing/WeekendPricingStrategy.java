package com.bms.bms_core.strategies.pricing;

import com.bms.bms_core.enums.SeatType;

public class WeekendPricingStrategy implements PricingStrategy{
    /** Applies weekend pricing rules to the seat's base price. */
    @Override
    public double calculatePrice(double basePrice, SeatType seatType) {
        return switch (seatType){
            case PREMIUM -> basePrice*9.0;
            case VIP -> basePrice*3.0;
            case RECLINER -> basePrice*1.5;
            case REGULAR -> basePrice*10.0;
            case SILVER, GOLD -> basePrice;
        };
    }
}
