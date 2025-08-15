package com.example.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Value
@Builder
public class BookingSummary {

    Long id;

    String bookingReference;

    String flightNumber;

    String originLocationCode;

    String destinationLocationCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;

    @JsonFormat(pattern = "HH:mm")
    LocalTime departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;

    String passengerName;

    String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime bookingTimestamp;

    BigDecimal totalAmount;

    String currency;

    Boolean roundTrip;

    Boolean internationalFlight;

    Boolean canBeCancelled;

    // 정적 팩토리 메서드
    public static BookingSummary from(com.example.flightbooking.model.Booking entity) {
        return BookingSummary.builder()
                .id(entity.getId())
                .bookingReference(entity.getBookingReference())
                .flightNumber(entity.getFlightNumber())
                .originLocationCode(entity.getOriginLocationCode())
                .destinationLocationCode(entity.getDestinationLocationCode())
                .departureDate(entity.getDepartureDate())
                .departureTime(entity.getDepartureTime())
                .returnDate(entity.getReturnDate())
                .passengerName(entity.getPassengerName())
                .status(entity.getStatus())
                .bookingTimestamp(entity.getBookingTimestamp())
                .totalAmount(entity.getTotalAmount())
                .currency(entity.getCurrency())
                .roundTrip(entity.isRoundTrip())
                .internationalFlight(entity.isInternationalFlight())
                .canBeCancelled(entity.canBeCancelled())
                .build();
    }
}