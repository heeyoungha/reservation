package com.example.flightbooking.service;

import com.example.flightbooking.dto.FlightSearchRequest;
import com.example.flightbooking.dto.FlightSearchResponse;
import com.example.flightbooking.model.FlightSearch;
import com.example.flightbooking.repository.FlightSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 통합 항공편 서비스
 * API 호출과 데이터베이스 저장을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {
    
    private final AmadeusService amadeusService;
    private final FlightSearchRepository flightSearchRepository;
    
    /**
     * 항공편 검색 (API 호출 + DB 저장)
     */
    public Mono<FlightSearchResponse> searchFlights(FlightSearchRequest request) {
        log.info("항공편 검색 시작: {} -> {}", 
                request.getOriginLocationCode(), request.getDestinationLocationCode());
        
        return amadeusService.searchFlights(request)
            .doOnSuccess(response -> {
                // 검색 결과를 데이터베이스에 저장
                saveSearchToDatabase(request, response);
            })
            .doOnError(error -> {
                log.error("항공편 검색 실패: {}", error.getMessage());
                // 에러 정보도 데이터베이스에 저장
                saveErrorToDatabase(request, error.getMessage());
            });
    }
    
    /**
     * 검색 결과를 데이터베이스에 저장
     */
    private void saveSearchToDatabase(FlightSearchRequest request, FlightSearchResponse response) {
        try {
            FlightSearch searchEntity = FlightSearch.builder()
                .originLocationCode(request.getOriginLocationCode())
                .destinationLocationCode(request.getDestinationLocationCode())
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .apiProvider(request.getApiProvider())
                .searchTimestamp(LocalDateTime.now())
                .searchResponse(response.toString()) // 간단한 응답 정보 저장
                .build();
            
            FlightSearch savedSearch = flightSearchRepository.save(searchEntity);
            log.info("검색 결과 저장 완료: ID = {}", savedSearch.getId());
            
        } catch (Exception e) {
            log.error("검색 결과 저장 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 에러 정보를 데이터베이스에 저장
     */
    private void saveErrorToDatabase(FlightSearchRequest request, String errorMessage) {
        try {
            FlightSearch searchEntity = FlightSearch.builder()
                .originLocationCode(request.getOriginLocationCode())
                .destinationLocationCode(request.getDestinationLocationCode())
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .adults(request.getAdults())
                .children(request.getChildren())
                .infants(request.getInfants())
                .apiProvider(request.getApiProvider())
                .searchTimestamp(LocalDateTime.now())
                .searchResponse("ERROR: " + errorMessage)
                .build();
            
            FlightSearch savedSearch = flightSearchRepository.save(searchEntity);
            log.info("에러 정보 저장 완료: ID = {}", savedSearch.getId());
            
        } catch (Exception e) {
            log.error("에러 정보 저장 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 저장된 검색 기록 조회
     */
    public Mono<FlightSearchResponse> getSearchHistory(String apiProvider) {
        log.info("검색 기록 조회: API Provider = {}", apiProvider);
        
        return Mono.fromCallable(() -> {
            var searches = flightSearchRepository.findByApiProviderOrderBySearchTimestampDesc(apiProvider);
            
            return FlightSearchResponse.builder()
                .apiProvider(apiProvider)
                .status("SUCCESS")
                .message("검색 기록 조회 완료 - " + searches.size() + "개 기록")
                .searchTimestamp(LocalDateTime.now())
                .flightOffers(java.util.List.of()) // 검색 기록에는 항공편 정보 없음
                .build();
        });
    }
} 