package com.example.flightbooking.controller;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Booking API", description = "항공편 예약 관리 API")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "새 예약 생성", description = "항공편 예약을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "예약 생성 성공",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "중복 예약 또는 비즈니스 규칙 위반"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        
        log.info("Creating booking request received: {}", request.getFlightNumber());
        
        try {
            BookingResponse response = bookingService.createBooking(request);
            log.info("Booking created successfully: {}", response.getBookingReference());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (BookingService.BookingException e) {
            log.warn("Booking creation failed: {}", e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        }
    }

    @Operation(summary = "예약 참조번호로 조회", description = "예약 참조번호를 사용하여 예약 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 조회 성공",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(
            @Parameter(description = "예약 참조번호", example = "BK12345678")
            @PathVariable String bookingReference) {
        
        log.info("Getting booking by reference: {}", bookingReference);
        
        return bookingService.getBookingByReference(bookingReference)
                .map(booking -> {
                    log.info("Booking found: {}", bookingReference);
                    return ResponseEntity.ok(booking);
                })
                .orElseGet(() -> {
                    log.warn("Booking not found: {}", bookingReference);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "예약 ID로 조회", description = "예약 ID를 사용하여 예약 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long id) {
        
        log.info("Getting booking by ID: {}", id);
        
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "이메일로 예약 목록 조회", description = "승객 이메일을 사용하여 예약 목록을 조회합니다.")
    @GetMapping("/email/{email}")
    public ResponseEntity<List<BookingResponse>> getBookingsByEmail(
            @Parameter(description = "승객 이메일", example = "passenger@example.com")
            @PathVariable String email) {
        
        log.info("Getting bookings by email: {}", email);
        
        List<BookingResponse> bookings = bookingService.getBookingsByEmail(email);
        log.info("Found {} bookings for email: {}", bookings.size(), email);
        
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "이메일과 이름으로 예약 목록 조회", description = "승객 이메일과 이름을 사용하여 예약 목록을 조회합니다. (예약번호를 모를 때)")
    @GetMapping("/search")
    public ResponseEntity<List<BookingResponse>> getBookingsByEmailAndName(
            @Parameter(description = "승객 이메일", example = "passenger@example.com")
            @RequestParam String email,
            @Parameter(description = "승객 이름", example = "홍길동")
            @RequestParam String name) {
        
        log.info("Getting bookings by email: {} and name: {}", email, name);
        
        List<BookingResponse> bookings = bookingService.getBookingsByEmailAndName(email, name);
        log.info("Found {} bookings for email: {} and name: {}", bookings.size(), email, name);
        
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "전체 예약 목록 조회 (페이징)", description = "페이징을 지원하는 전체 예약 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Getting all bookings with pagination: {}", pageable);
        
        Page<BookingResponse> bookings = bookingService.getAllBookings(pageable);
        log.info("Retrieved {} bookings (page {} of {})", 
                bookings.getNumberOfElements(), 
                bookings.getNumber() + 1, 
                bookings.getTotalPages());
        
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "예약 취소", description = "예약을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 취소 성공",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "취소할 수 없는 예약 상태")
    })
    @PutMapping("/{bookingReference}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @Parameter(description = "예약 참조번호", example = "BK12345678")
            @PathVariable String bookingReference) {
        
        log.info("Cancelling booking: {}", bookingReference);
        
        try {
            BookingResponse response = bookingService.cancelBooking(bookingReference);
            log.info("Booking cancelled successfully: {}", bookingReference);
            
            return ResponseEntity.ok(response);
            
        } catch (BookingService.BookingNotFoundException e) {
            log.warn("Booking not found for cancellation: {}", bookingReference);
            return ResponseEntity.notFound().build();
            
        } catch (BookingService.BookingException e) {
            log.warn("Booking cancellation failed: {}", e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        }
    }

    @Operation(summary = "예약 상태 변경", description = "예약의 상태를 변경합니다.")
    @PutMapping("/{bookingReference}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @Parameter(description = "예약 참조번호", example = "BK12345678")
            @PathVariable String bookingReference,
            @Parameter(description = "새 상태", example = "CONFIRMED")
            @RequestParam String status) {
        
        log.info("Updating booking status: {} to {}", bookingReference, status);
        
        try {
            BookingResponse response = bookingService.updateBookingStatus(bookingReference, status);
            log.info("Booking status updated successfully: {} -> {}", bookingReference, status);
            
            return ResponseEntity.ok(response);
            
        } catch (BookingService.BookingNotFoundException e) {
            log.warn("Booking not found for status update: {}", bookingReference);
            return ResponseEntity.notFound().build();
            
        } catch (BookingService.BookingException e) {
            log.warn("Booking status update failed: {}", e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리
        }
    }

    // === 관리자용 엔드포인트들 ===

    @Operation(summary = "예약 통계 조회", description = "예약 상태별 통계를 조회합니다.")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        log.info("Getting booking statistics");
        
        // 간단한 통계 정보 제공
        Map<String, Object> statistics = Map.of(
                "totalBookings", bookingService.getAllBookings(Pageable.unpaged()).getTotalElements(),
                "message", "더 자세한 통계는 관리자 대시보드에서 확인하세요"
        );
        
        return ResponseEntity.ok(statistics);
    }

    // === 헬스체크 및 테스트용 엔드포인트 ===

    @Operation(summary = "예약 시스템 헬스체크", description = "예약 시스템의 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.debug("Booking system health check");
        
        Map<String, String> health = Map.of(
                "status", "UP",
                "service", "BookingService",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(health);
    }

    @Operation(summary = "테스트용 예약 데이터 생성", description = "개발/테스트 환경에서 샘플 예약 데이터를 생성합니다.")
    @PostMapping("/test-data")
    public ResponseEntity<Map<String, String>> createTestBooking() {
        log.info("Creating test booking data");
        
        try {
            // 테스트용 예약 요청 생성
            BookingRequest testRequest = BookingRequest.builder()
                    .flightNumber("KE123")
                    .originLocationCode("ICN")
                    .destinationLocationCode("LAX")
                    .departureDate(java.time.LocalDate.now().plusDays(7))
                    .departureTime(java.time.LocalTime.of(14, 30))
                    .passengerName("테스트 승객")
                    .passengerEmail("test@example.com")
                    .passengerPhone("010-1234-5678")
                    .apiProvider("Amadeus")
                    .totalAmount(java.math.BigDecimal.valueOf(1200.50))
                    .currency("USD")
                    .build();

            BookingResponse response = bookingService.createBooking(testRequest);
            
            Map<String, String> result = Map.of(
                    "message", "테스트 예약이 생성되었습니다",
                    "bookingReference", response.getBookingReference(),
                    "status", response.getStatus()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            log.error("Failed to create test booking", e);
            
            Map<String, String> error = Map.of(
                    "message", "테스트 예약 생성 실패",
                    "error", e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 