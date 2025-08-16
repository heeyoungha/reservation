package com.example.flightbooking.repository;

import com.example.flightbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByApiProviderOrderByBookingTimestampDesc(String apiProvider);
    
    List<Booking> findByPassengerEmailOrderByBookingTimestampDesc(String passengerEmail);
    
    // 이메일과 승객명으로 조회 (예약번호를 모를 때)
    List<Booking> findByPassengerEmailAndPassengerNameOrderByBookingTimestampDesc(String passengerEmail, String passengerName);
    
    List<Booking> findByStatusOrderByBookingTimestampDesc(String status);
    
    // 중복 예약 확인용
    List<Booking> findByPassengerEmailAndFlightNumberAndDepartureDate(
            String passengerEmail, String flightNumber, LocalDate departureDate);
    
    // 특정 항공편의 예약 목록
    List<Booking> findByFlightNumberAndDepartureDateOrderByBookingTimestampDesc(
            String flightNumber, LocalDate departureDate);
    
    // 특정 기간의 예약 목록
    @Query("SELECT b FROM Booking b WHERE b.departureDate BETWEEN :startDate AND :endDate ORDER BY b.departureDate, b.departureTime")
    List<Booking> findBookingsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // 상태별 개수 조회
    long countByStatus(String status);
}
