package com.example.flightbooking.controller;

import com.example.flightbooking.dto.FlightSearchRequest;
import com.example.flightbooking.dto.FlightSearchResponse;
import com.example.flightbooking.service.AmadeusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Amadeus API 실제 호출 테스트를 위한 컨트롤러
 * 실제 프로덕션에서는 제거하거나 적절한 테스트 코드로 대체
 */
@RestController
@RequestMapping("/api/amadeus-test")
@RequiredArgsConstructor
@Slf4j
public class AmadeusApiTestController {
    
    private final AmadeusService amadeusService;
    
    /**
     * Amadeus API 인증 토큰 발급 테스트
     */
    @GetMapping("/auth")
    public ResponseEntity<String> testAuth() {
        log.info("Amadeus API 인증 테스트 시작");
        
        return amadeusService.getAccessToken()
            .map(token -> {
                log.info("인증 토큰 발급 성공: {}", token.substring(0, 20) + "...");
                return ResponseEntity.ok("✅ Amadeus API 인증 성공\n토큰: " + token.substring(0, 20) + "...");
            })
            .onErrorReturn(ResponseEntity.status(500)
                .body("❌ Amadeus API 인증 실패\n에러: 인증 토큰 발급 중 오류가 발생했습니다."))
            .block();
    }
    
    /**
     * 실제 항공편 검색 테스트
     */
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> testFlightSearch(@RequestBody FlightSearchRequest request) {
        log.info("Amadeus API 항공편 검색 테스트 시작");
        
        return amadeusService.searchFlights(request)
            .map(response -> {
                log.info("항공편 검색 성공: {}", response);
                return ResponseEntity.ok(response);
            })
            .onErrorReturn(ResponseEntity.status(500)
                .body(FlightSearchResponse.builder()
                    .apiProvider("AMADEUS")
                    .status("ERROR")
                    .message("항공편 검색 중 오류가 발생했습니다.")
                    .build()))
            .block();
    }
    
    /**
     * 간단한 항공편 검색 테스트 (ICN -> LAX)
     */
    @GetMapping("/search-simple")
    public ResponseEntity<FlightSearchResponse> testSimpleSearch() {
        log.info("간단한 항공편 검색 테스트 시작");
        
        FlightSearchRequest request = FlightSearchRequest.builder()
            .originLocationCode("ICN")
            .destinationLocationCode("LAX")
            .departureDate(LocalDate.now().plusDays(30))
            .adults(1)
            .children(0)
            .infants(0)
            .apiProvider("AMADEUS")
            .build();
        
        return amadeusService.searchFlights(request)
            .map(response -> {
                log.info("간단한 항공편 검색 성공: {}", response);
                return ResponseEntity.ok(response);
            })
            .onErrorReturn(ResponseEntity.status(500)
                .body(FlightSearchResponse.builder()
                    .apiProvider("AMADEUS")
                    .status("ERROR")
                    .message("간단한 항공편 검색 중 오류가 발생했습니다.")
                    .build()))
            .block();
    }
} 