package com.bms.bms_core.dto.response;

import com.bms.bms_core.enums.SeatStatus;
import com.bms.bms_core.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusResponse {
    private String seatNumber;
    private SeatStatus seatStatus;
    private String showId;
    private String theaterId;
    private String hallId;
    private SeatType seatType;

    /** Creates a builder for assembling a seat-status response. */
    public static SeatStatusResponseBuilder builder() {
        return new SeatStatusResponseBuilder();
    }

    public static class SeatStatusResponseBuilder {
        private String seatNumber;
        private SeatStatus seatStatus;
        private String showId;
        private String theaterId;
        private String hallId;
        private SeatType seatType;

        /** Sets the seat label on the response being built. */
        public SeatStatusResponseBuilder seatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }

        /** Sets the availability status on the response being built. */
        public SeatStatusResponseBuilder seatStatus(SeatStatus seatStatus) {
            this.seatStatus = seatStatus;
            return this;
        }

        /** Sets the associated show ID on the response being built. */
        public SeatStatusResponseBuilder showId(String showId) {
            this.showId = showId;
            return this;
        }

        /** Sets the associated theater ID on the response being built. */
        public SeatStatusResponseBuilder theaterId(String theaterId) {
            this.theaterId = theaterId;
            return this;
        }

        /** Sets the associated hall ID on the response being built. */
        public SeatStatusResponseBuilder hallId(String hallId) {
            this.hallId = hallId;
            return this;
        }

        /** Sets the seat category on the response being built. */
        public SeatStatusResponseBuilder seatType(SeatType seatType) {
            this.seatType = seatType;
            return this;
        }

        /** Builds the requested resource from the configured data. */
        public SeatStatusResponse build() {
            return new SeatStatusResponse(seatNumber, seatStatus, showId, theaterId, hallId, seatType);
        }
    }
}
