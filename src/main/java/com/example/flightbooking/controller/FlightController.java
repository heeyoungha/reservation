package com.example.flightbooking.controller;

import com.example.flightbooking.dto.FlightSearchRequest;
import com.example.flightbooking.dto.FlightSearchResponse;
import com.example.flightbooking.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 항공편 검색 API 컨트롤러
 * 실제 프로덕션용 API 엔드포인트
 */
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {
    
    private final FlightService flightService;
    
    /**
     * 항공편 검색 API
     */
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        log.info("항공편 검색 API 호출: {} -> {}", 
                request.getOriginLocationCode(), request.getDestinationLocationCode());
        
        return flightService.searchFlights(request)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(500)
                .body(FlightSearchResponse.builder()
                    .apiProvider(request.getApiProvider())
                    .status("ERROR")
                    .message("항공편 검색 중 오류가 발생했습니다.")
                    .searchTimestamp(java.time.LocalDateTime.now())
                    .flightOffers(java.util.List.of())
                    .build()))
            .block();
    }
    
    /**
     * 검색 기록 조회 API
     */
    @GetMapping("/search-history/{apiProvider}")
    public ResponseEntity<FlightSearchResponse> getSearchHistory(@PathVariable String apiProvider) {
        log.info("검색 기록 조회 API 호출: API Provider = {}", apiProvider);
        
        return flightService.getSearchHistory(apiProvider)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(500)
                .body(FlightSearchResponse.builder()
                    .apiProvider(apiProvider)
                    .status("ERROR")
                    .message("검색 기록 조회 중 오류가 발생했습니다.")
                    .searchTimestamp(java.time.LocalDateTime.now())
                    .flightOffers(java.util.List.of())
                    .build()))
            .block();
    }
    
    /**
     * 간단한 항공편 검색 테스트 (GET 요청)
     */
    @GetMapping("/search-simple")
    public ResponseEntity<FlightSearchResponse> searchFlightsSimple(
            @RequestParam(defaultValue = "ICN") String origin,
            @RequestParam(defaultValue = "LAX") String destination,
            @RequestParam(defaultValue = "AMADEUS") String apiProvider) {
        
        log.info("간단한 항공편 검색 API 호출: {} -> {} (Provider: {})", origin, destination, apiProvider);
        
        FlightSearchRequest request = FlightSearchRequest.builder()
            .originLocationCode(origin)
            .destinationLocationCode(destination)
            .departureDate(java.time.LocalDate.now().plusDays(30))
            .adults(1)
            .children(0)
            .infants(0)
            .apiProvider(apiProvider)
            .build();
        
        return flightService.searchFlights(request)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(500)
                .body(FlightSearchResponse.builder()
                    .apiProvider(apiProvider)
                    .status("ERROR")
                    .message("간단한 항공편 검색 중 오류가 발생했습니다.")
                    .searchTimestamp(java.time.LocalDateTime.now())
                    .flightOffers(java.util.List.of())
                    .build()))
            .block();
    }
} 