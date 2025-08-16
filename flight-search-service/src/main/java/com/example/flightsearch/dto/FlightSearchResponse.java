package com.example.flightsearch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값 제외
public class FlightSearchResponse {
    
    Long id;
    
    String originLocationCode;
    
    String destinationLocationCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    
    Integer adults;
    
    Integer children;
    
    Integer infants;
    
    String apiProvider;
    
    String status;
    
    String message;
    
    List<FlightOffer> flightOffers;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime searchTimestamp;
    
    // 검색 응답은 민감할 수 있으므로 별도 DTO로 분리하거나 제외
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String searchResponse;
    
    // 계산된 필드들
    Boolean roundTrip;
    
    Integer totalPassengers;
    
    Boolean internationalFlight;
    
    // 정적 팩토리 메서드 (Entity -> DTO 변환)
    public static FlightSearchResponse from(com.example.flightsearch.model.FlightSearch entity) {
        return FlightSearchResponse.builder()
            .id(entity.getId())
            .originLocationCode(entity.getOriginLocationCode())
            .destinationLocationCode(entity.getDestinationLocationCode())
            .departureDate(entity.getDepartureDate())
            .returnDate(entity.getReturnDate())
            .adults(entity.getAdults())
            .children(entity.getChildren())
            .infants(entity.getInfants())
            .apiProvider(entity.getApiProvider())
            .searchTimestamp(entity.getSearchTimestamp())
            .searchResponse(entity.getSearchResponse())
            .roundTrip(entity.isRoundTrip())
            .totalPassengers(entity.getTotalPassengers())
            .internationalFlight(entity.isInternationalFlight())
            .build();
    }
    
    @Value
    @Builder
    public static class FlightOffer {
        String id;
        String airline;
        String flightNumber;
        String originLocationCode;
        String destinationLocationCode;
        String departureDate;
        String departureTime;
        String arrivalDate;
        String arrivalTime;
        String duration;
        String cabinClass;
        Price price;
        Integer availableSeats;
    }
    
    @Value
    @Builder
    public static class Price {
        String currency;
        Double total;
        Double base;
        Double taxes;
    }
}