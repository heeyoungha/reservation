package com.example.flightbooking.repository;

import com.example.flightbooking.model.FlightSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightSearchRepository extends JpaRepository<FlightSearch, Long> {
    
    List<FlightSearch> findByApiProviderOrderBySearchTimestampDesc(String apiProvider);
    
    List<FlightSearch> findByOriginLocationCodeAndDestinationLocationCodeOrderBySearchTimestampDesc(
        String originLocationCode, String destinationLocationCode);
}
