package com.example.flightbooking.repository;

import com.example.flightbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByApiProviderOrderByBookingTimestampDesc(String apiProvider);
    
    List<Booking> findByPassengerEmailOrderByBookingTimestampDesc(String passengerEmail);
    
    List<Booking> findByStatusOrderByBookingTimestampDesc(String status);
}
