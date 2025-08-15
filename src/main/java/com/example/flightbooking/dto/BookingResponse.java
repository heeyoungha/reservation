package com.example.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

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

    @JsonFormat(pattern = "HH:mm")
    LocalTime returnTime;

    String passengerName;

    String passengerEmail;

    String passengerPhone;

    String apiProvider;

    String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime bookingTimestamp;

    BigDecimal totalAmount;

    String currency;

    // 검색 응답은 민감할 수 있으므로 별도 처리
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String bookingResponse;

    // 계산된 필드들
    Boolean roundTrip;

    Boolean confirmed;

    Boolean cancelled;

    Boolean pending;

    Boolean canBeCancelled;

    Boolean internationalFlight;

    Boolean pastDeparture;

    // 정적 팩토리 메서드 (Entity -> DTO 변환)
    public static BookingResponse from(com.example.flightbooking.model.Booking entity) {
        return BookingResponse.builder()
                .id(entity.getId())
                .bookingReference(entity.getBookingReference())
                .flightNumber(entity.getFlightNumber())
                .originLocationCode(entity.getOriginLocationCode())
                .destinationLocationCode(entity.getDestinationLocationCode())
                .departureDate(entity.getDepartureDate())
                .departureTime(entity.getDepartureTime())
                .returnDate(entity.getReturnDate())
                .returnTime(entity.getReturnTime())
                .passengerName(entity.getPassengerName())
                .passengerEmail(entity.getPassengerEmail())
                .passengerPhone(entity.getPassengerPhone())
                .apiProvider(entity.getApiProvider())
                .status(entity.getStatus())
                .bookingTimestamp(entity.getBookingTimestamp())
                .totalAmount(entity.getTotalAmount())
                .currency(entity.getCurrency())
                .bookingResponse(entity.getBookingResponse())
                .roundTrip(entity.isRoundTrip())
                .confirmed(entity.isConfirmed())
                .cancelled(entity.isCancelled())
                .pending(entity.isPending())
                .canBeCancelled(entity.canBeCancelled())
                .internationalFlight(entity.isInternationalFlight())
                .pastDeparture(entity.isPastDeparture())
                .build();
    }
}