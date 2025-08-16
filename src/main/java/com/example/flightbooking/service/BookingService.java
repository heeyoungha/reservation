package com.example.flightbooking.service;

import com.example.flightbooking.dto.BookingRequest;
import com.example.flightbooking.dto.BookingResponse;
import com.example.flightbooking.dto.FlightSearchResponse;
import com.example.flightbooking.model.Booking;
import com.example.flightbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightService flightService;

    /**
     * 새 예약 생성
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for flight: {} from {} to {}", 
                request.getFlightNumber(), request.getOriginLocationCode(), request.getDestinationLocationCode());

        try {
            // 1. 비즈니스 로직 검증
            validateBookingRequest(request);

            // 2. 항공편 존재 여부 확인 (실제 API 호출)
            validateFlightAvailability(request);

            // 3. 중복 예약 확인
            checkDuplicateBooking(request);

            // 4. 예약 엔티티 생성
            Booking booking = createBookingEntity(request);

            // 5. 외부 API 호출 시뮬레이션 (실제로는 Amadeus/Sabre API 호출)
            simulateExternalBookingApi(booking, request);

            // 6. 데이터베이스 저장
            Booking savedBooking = bookingRepository.save(booking);

            log.info("Booking created successfully: {}", savedBooking.getBookingReference());
            return BookingResponse.from(savedBooking);

        } catch (Exception e) {
            log.error("Failed to create booking for flight: {}", request.getFlightNumber(), e);
            
            // 실패한 예약 기록도 저장 (문제 추적용)
            Booking failedBooking = createBookingEntity(request);
            failedBooking.setStatus("FAILED");
            failedBooking.setBookingResponse("Booking failed: " + e.getMessage());
            bookingRepository.save(failedBooking);
            
            throw new BookingException("예약 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 예약 참조번호로 조회
     */
    public Optional<BookingResponse> getBookingByReference(String bookingReference) {
        log.info("Retrieving booking by reference: {}", bookingReference);
        
        return bookingRepository.findByBookingReference(bookingReference)
                .map(BookingResponse::from);
    }

    /**
     * 예약 ID로 조회
     */
    public Optional<BookingResponse> getBookingById(Long id) {
        log.info("Retrieving booking by ID: {}", id);
        
        return bookingRepository.findById(id)
                .map(BookingResponse::from);
    }

    /**
     * 승객 이메일로 예약 목록 조회
     */
    public List<BookingResponse> getBookingsByEmail(String email) {
        log.info("Retrieving bookings for email: {}", email);
        
        return bookingRepository.findByPassengerEmailOrderByBookingTimestampDesc(email)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    /**
     * 승객 이메일과 이름으로 예약 목록 조회 (예약번호를 모를 때)
     */
    public List<BookingResponse> getBookingsByEmailAndName(String email, String name) {
        log.info("Retrieving bookings for email: {} and name: {}", email, name);
        
        return bookingRepository.findByPassengerEmailAndPassengerNameOrderByBookingTimestampDesc(email, name)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    /**
     * 페이징된 예약 목록 조회
     */
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        log.info("Retrieving all bookings with pagination: {}", pageable);
        
        return bookingRepository.findAll(pageable)
                .map(BookingResponse::from);
    }

    /**
     * 예약 취소
     */
    @Transactional
    public BookingResponse cancelBooking(String bookingReference) {
        log.info("Cancelling booking: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("예약을 찾을 수 없습니다: " + bookingReference));

        // 취소 가능 여부 확인
        if (!booking.canBeCancelled()) {
            throw new BookingException("취소할 수 없는 예약입니다. 현재 상태: " + booking.getStatus());
        }

        // 출발 시간 확인 (출발 24시간 전까지만 취소 가능)
        if (booking.isPastDeparture()) {
            throw new BookingException("이미 출발한 항공편은 취소할 수 없습니다.");
        }

        try {
            // 외부 API 취소 호출 시뮬레이션
            simulateExternalCancellationApi(booking);

            // 상태 업데이트
            booking.setStatus("CANCELLED");
            booking.setBookingResponse(booking.getBookingResponse() + "\nCancelled at: " + LocalDateTime.now());

            Booking cancelledBooking = bookingRepository.save(booking);
            
            log.info("Booking cancelled successfully: {}", bookingReference);
            return BookingResponse.from(cancelledBooking);

        } catch (Exception e) {
            log.error("Failed to cancel booking: {}", bookingReference, e);
            throw new BookingException("예약 취소에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 예약 상태 업데이트
     */
    @Transactional
    public BookingResponse updateBookingStatus(String bookingReference, String newStatus) {
        log.info("Updating booking status: {} to {}", bookingReference, newStatus);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException("예약을 찾을 수 없습니다: " + bookingReference));

        // 상태 변경 유효성 검증
        validateStatusChange(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking status updated successfully: {} -> {}", bookingReference, newStatus);
        return BookingResponse.from(updatedBooking);
    }

    // === Private Helper Methods ===

    private void validateBookingRequest(BookingRequest request) {
        // 출발일이 과거인지 확인
        if (request.getDepartureDate().isBefore(java.time.LocalDate.now())) {
            throw new BookingException("출발일은 오늘 이후여야 합니다.");
        }

        // 왕복일 경우 복귀일 검증
        if (request.isRoundTrip() && request.getReturnDate().isBefore(request.getDepartureDate())) {
            throw new BookingException("복귀일은 출발일 이후여야 합니다.");
        }

        // 금액 검증
        if (request.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BookingException("예약 금액은 0보다 커야 합니다.");
        }
    }

    private void validateFlightAvailability(BookingRequest request) {
        try {
            // 실제 항공편 검색 API 호출해서 존재하는지 확인
            FlightSearchResponse searchResponse = flightService.searchFlights(
                    com.example.flightbooking.dto.FlightSearchRequest.builder()
                            .originLocationCode(request.getOriginLocationCode())
                            .destinationLocationCode(request.getDestinationLocationCode())
                            .departureDate(request.getDepartureDate())
                            .adults(1)
                            .build()
            ).block(); // Mono를 동기적으로 변환

            if (searchResponse != null && searchResponse.getFlightOffers() != null) {
                boolean flightExists = searchResponse.getFlightOffers().stream()
                        .anyMatch(flight -> flight.getFlightNumber().equals(request.getFlightNumber()));

                if (!flightExists) {
                    throw new BookingException("해당 항공편을 찾을 수 없습니다: " + request.getFlightNumber());
                }
            }

        } catch (Exception e) {
            log.warn("Could not validate flight availability: {}", e.getMessage());
            // 항공편 검색 실패 시에도 예약은 진행 (실제 운영에서는 더 엄격하게 처리)
        }
    }

    private void checkDuplicateBooking(BookingRequest request) {
        // 동일한 승객이 같은 항공편에 대한 중복 예약 확인
        List<Booking> existingBookings = bookingRepository.findByPassengerEmailAndFlightNumberAndDepartureDate(
                request.getPassengerEmail(),
                request.getFlightNumber(),
                request.getDepartureDate()
        );

        boolean hasPendingOrConfirmedBooking = existingBookings.stream()
                .anyMatch(booking -> "PENDING".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus()));

        if (hasPendingOrConfirmedBooking) {
            throw new BookingException("이미 해당 항공편에 대한 예약이 존재합니다.");
        }
    }

    private Booking createBookingEntity(BookingRequest request) {
        return Booking.builder()
                .flightNumber(request.getFlightNumber())
                .originLocationCode(request.getOriginLocationCode())
                .destinationLocationCode(request.getDestinationLocationCode())
                .departureDate(request.getDepartureDate())
                .departureTime(request.getDepartureTime())
                .returnDate(request.getReturnDate())
                .returnTime(request.getReturnTime())
                .passengerName(request.getPassengerName())
                .passengerEmail(request.getPassengerEmail())
                .passengerPhone(request.getPassengerPhone())
                .apiProvider(request.getApiProvider())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .bookingTimestamp(LocalDateTime.now())
                .build();
    }

    private void simulateExternalBookingApi(Booking booking, BookingRequest request) throws InterruptedException {
        // 외부 API 호출 시뮬레이션 (실제로는 Amadeus/Sabre API 호출)
        log.info("Calling external booking API for provider: {}", request.getApiProvider());
        
        // API 호출 지연 시뮬레이션
        Thread.sleep(1000);
        
        // 외부 API 호출 성공 시뮬레이션 (항상 성공)

        // 성공한 경우 응답 시뮬레이션
        String apiResponse = String.format(
                "Booking confirmed by %s API at %s. PNR: %s",
                request.getApiProvider(),
                LocalDateTime.now(),
                booking.getBookingReference()
        );
        
        booking.setStatus("CONFIRMED");
        booking.setBookingResponse(apiResponse);
    }

    private void simulateExternalCancellationApi(Booking booking) throws InterruptedException {
        log.info("Calling external cancellation API for provider: {}", booking.getApiProvider());
        
        // API 호출 지연 시뮬레이션
        Thread.sleep(500);
        
        // 95% 성공률 시뮬레이션
        if (Math.random() < 0.05) {
            throw new BookingException("외부 취소 API 호출 실패");
        }
    }

    private void validateStatusChange(String currentStatus, String newStatus) {
        // 상태 변경 규칙 정의
        boolean isValidChange = switch (currentStatus) {
            case "PENDING" -> "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus) || "FAILED".equals(newStatus);
            case "CONFIRMED" -> "CANCELLED".equals(newStatus);
            case "CANCELLED", "FAILED" -> false; // 최종 상태에서는 변경 불가
            default -> false;
        };

        if (!isValidChange) {
            throw new BookingException(
                    String.format("잘못된 상태 변경입니다: %s -> %s", currentStatus, newStatus)
            );
        }
    }

    // === Exception Classes ===
    
    public static class BookingException extends RuntimeException {
        public BookingException(String message) {
            super(message);
        }
        
        public BookingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) {
            super(message);
        }
    }
} 