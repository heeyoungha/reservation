package com.example.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class FlightSearchSummaryDto {

    Long id;

    String originLocationCode;

    String destinationLocationCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;

    Integer totalPassengers;

    String apiProvider;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime searchTimestamp;

    Boolean roundTrip;

    Boolean internationalFlight;

    // 정적 팩토리 메서드
    public static FlightSearchSummaryDto from(com.example.flightsearch.model.FlightSearch entity) {
        return FlightSearchSummaryDto.builder()
                .id(entity.getId())
                .originLocationCode(entity.getOriginLocationCode())
                .destinationLocationCode(entity.getDestinationLocationCode())
                .departureDate(entity.getDepartureDate())
                .returnDate(entity.getReturnDate())
                .totalPassengers(entity.getTotalPassengers())
                .apiProvider(entity.getApiProvider())
                .searchTimestamp(entity.getSearchTimestamp())
                .roundTrip(entity.isRoundTrip())
                .internationalFlight(entity.isInternationalFlight())
                .build();
    }
}