package com.example.flightbooking.controller;

import com.example.flightbooking.dto.FlightSearchRequest;
import com.example.flightbooking.dto.BookingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * DTO 테스트를 위한 임시 컨트롤러
 * 실제 프로덕션에서는 제거하거나 적절한 테스트 코드로 대체
 */
@RestController
@RequestMapping("/api/dto-test")
@RequiredArgsConstructor
@Slf4j
public class DtoTestController {

    /**
     * FlightSearchRequest DTO 테스트
     */
    @PostMapping("/flight-search")
    public ResponseEntity<String> testFlightSearchRequest(@Valid @RequestBody FlightSearchRequest request) {
        log.info("FlightSearchRequest 테스트: {}", request);
        
        StringBuilder result = new StringBuilder();
        result.append("✅ FlightSearchRequest 검증 성공\n");
        result.append("출발지: ").append(request.getOriginLocationCode()).append("\n");
        result.append("도착지: ").append(request.getDestinationLocationCode()).append("\n");
        result.append("출발일: ").append(request.getDepartureDate()).append("\n");
        result.append("왕복여부: ").append(request.isRoundTrip()).append("\n");
        result.append("총 승객수: ").append(request.getTotalPassengers()).append("\n");
        result.append("API 제공자: ").append(request.getApiProvider());
        
        return ResponseEntity.ok(result.toString());
    }

    /**
     * BookingRequest DTO 테스트
     */
    @PostMapping("/booking")
    public ResponseEntity<String> testBookingRequest(@Valid @RequestBody BookingRequest request) {
        log.info("BookingRequest 테스트: {}", request);
        
        StringBuilder result = new StringBuilder();
        result.append("✅ BookingRequest 검증 성공\n");
        result.append("항공편: ").append(request.getFlightNumber()).append("\n");
        result.append("출발지: ").append(request.getOriginLocationCode()).append("\n");
        result.append("도착지: ").append(request.getDestinationLocationCode()).append("\n");
        result.append("출발일: ").append(request.getDepartureDate()).append("\n");
        result.append("출발시간: ").append(request.getDepartureTime()).append("\n");
        result.append("왕복여부: ").append(request.isRoundTrip()).append("\n");
        result.append("승객명: ").append(request.getPassengerName()).append("\n");
        result.append("이메일: ").append(request.getPassengerEmail()).append("\n");
        result.append("전화번호: ").append(request.getPassengerPhone()).append("\n");
        result.append("API 제공자: ").append(request.getApiProvider()).append("\n");
        result.append("총 금액: ").append(request.getTotalAmount()).append(" ").append(request.getCurrency());
        
        return ResponseEntity.ok(result.toString());
    }

    /**
     * 잘못된 FlightSearchRequest로 validation 테스트
     */
    @PostMapping("/flight-search/invalid")
    public ResponseEntity<String> testInvalidFlightSearchRequest(@RequestBody FlightSearchRequest request) {
        log.info("잘못된 FlightSearchRequest 테스트: {}", request);
        
        // validation이 실패하면 여기까지 오지 않음
        return ResponseEntity.ok("예상치 못한 성공 (validation이 실패해야 함)");
    }

    /**
     * 잘못된 BookingRequest로 validation 테스트
     */
    @PostMapping("/booking/invalid")
    public ResponseEntity<String> testInvalidBookingRequest(@RequestBody BookingRequest request) {
        log.info("잘못된 BookingRequest 테스트: {}", request);
        
        // validation이 실패하면 여기까지 오지 않음
        return ResponseEntity.ok("예상치 못한 성공 (validation이 실패해야 함)");
    }
} 