package com.example.flightbooking.controller;

import com.example.flightbooking.model.FlightSearch;
import com.example.flightbooking.repository.FlightSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 데이터베이스 연결 및 엔티티 테스트를 위한 컨트롤러
 * 실제 프로덕션에서는 제거해야 함
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseTestController {

    private final FlightSearchRepository flightSearchRepository;

    /**
     * 테스트 1: 검색 로그 생성 테스트
     * - 엔티티가 데이터베이스에 정상적으로 저장되는지 확인
     * - Lombok @Value 어노테이션이 정상 작동하는지 확인
     * - 비즈니스 로직 메서드들이 정상 작동하는지 확인
     * 
     * @param search 생성할 검색 로그 데이터
     * @return 저장된 검색 로그 (ID 포함)
     */
    @PostMapping("/search")
    public FlightSearch createTestSearch(@RequestBody FlightSearch search) {
        log.info("Creating test search: {}", search);
        return flightSearchRepository.save(search);
    }

    /**
     * 테스트 2: 모든 검색 로그 조회 테스트
     * - 데이터베이스에서 모든 검색 로그를 조회
     * - JPA Repository가 정상 작동하는지 확인
     * - JSON 직렬화가 정상 작동하는지 확인
     * 
     * @return 모든 검색 로그 목록
     */
    @GetMapping("/searches")
    public List<FlightSearch> getAllSearches() {
        log.info("Fetching all searches");
        return flightSearchRepository.findAll();
    }

    /**
     * 테스트 3: API별 검색 로그 조회 테스트
     * - 특정 API 제공자(AMADEUS, SABRE)별로 검색 로그 조회
     * - 커스텀 Repository 메서드가 정상 작동하는지 확인
     * - 정렬 기능이 정상 작동하는지 확인
     * 
     * @param apiProvider API 제공자 (AMADEUS, SABRE)
     * @return 해당 API 제공자의 검색 로그 목록 (최신순 정렬)
     */
    @GetMapping("/searches/{apiProvider}")
    public List<FlightSearch> getSearchesByProvider(@PathVariable String apiProvider) {
        log.info("Fetching searches for provider: {}", apiProvider);
        return flightSearchRepository.findByApiProviderOrderBySearchTimestampDesc(apiProvider);
    }
    
    /**
     * 테스트 4: 검색 유효성 검증 테스트
     * - 특정 검색 로그의 비즈니스 로직 검증
     * - isRoundTrip(): 왕복 여부 확인
     * - getTotalPassengers(): 총 승객 수 계산
     * - isValidSearch(): 유효한 검색인지 확인 (미래 날짜, 출발지≠도착지)
     * - isInternationalFlight(): 국제선 여부 확인
     * 
     * @param id 검색 로그 ID
     * @return 검색 로그의 상세 유효성 정보
     */
    @GetMapping("/searches/validate/{id}")
    public String validateSearch(@PathVariable Long id) {
        FlightSearch search = flightSearchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Search not found"));
        
        return "Search ID: " + search.getId() + "
" +
               "Valid Search: " + search.isValidSearch() + "
" +
               "Round Trip: " + search.isRoundTrip() + "
" +
               "Total Passengers: " + search.getTotalPassengers() + "
" +
               "International: " + search.isInternationalFlight();
    }
}
