package com.example.flightsearch.service;

import com.example.flightsearch.config.AmadeusConfig;
import com.example.flightsearch.dto.FlightSearchRequest;
import com.example.flightsearch.dto.FlightSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Amadeus API 호출 서비스
 * 실제 Amadeus API와 통신하여 항공편 검색 및 예약 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusService {
    
    private final AmadeusConfig amadeusConfig;
    private final WebClient amadeusWebClient;
    
    /**
     * Amadeus API 인증 토큰 발급
     */
    public Mono<String> getAccessToken() {
        log.info("Amadeus API 인증 토큰 발급 시작");
        
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("grant_type", "client_credentials");
        authRequest.put("client_id", amadeusConfig.getClientId());
        authRequest.put("client_secret", amadeusConfig.getClientSecret());
        
        return WebClient.create()
            .post()
            .uri(amadeusConfig.getAuthUrl())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue(buildFormData(authRequest))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
            .map(response -> {
                String accessToken = (String) response.get("access_token");
                log.info("Amadeus API 인증 토큰 발급 성공");
                return accessToken;
            })
            .doOnError(error -> log.error("Amadeus API 인증 실패: {}", error.getMessage()));
    }
    
    /**
     * 항공편 검색
     */
    public Mono<FlightSearchResponse> searchFlights(FlightSearchRequest request) {
        log.info("Amadeus API 항공편 검색 시작: {} -> {}", 
                request.getOriginLocationCode(), request.getDestinationLocationCode());
        
        return getAccessToken()
            .flatMap(token -> {
                String searchUrl = amadeusConfig.getFlightOffersSearchUrl() + 
                    "?originLocationCode=" + request.getOriginLocationCode() +
                    "&destinationLocationCode=" + request.getDestinationLocationCode() +
                    "&departureDate=" + request.getDepartureDate() +
                    "&adults=" + request.getAdults() +
                    "&children=" + request.getChildren() +
                    "&infants=" + request.getInfants() +
                    "&max=10";
                
                if (request.isRoundTrip()) {
                    searchUrl += "&returnDate=" + request.getReturnDate();
                }
                
                return amadeusWebClient
                    .get()
                    .uri(searchUrl)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
                    .map(this::convertToFlightSearchResponse)
                    .doOnSuccess(response -> log.info("Amadeus API 항공편 검색 성공: {}개 항공편", 
                            response.getFlightOffers().size()))
                    .doOnError(error -> log.error("Amadeus API 항공편 검색 실패: {}", error.getMessage()));
            });
    }
    
    /**
     * Amadeus API 응답을 FlightSearchResponse로 변환
     */
    private FlightSearchResponse convertToFlightSearchResponse(LinkedHashMap<String, Object> amadeusResponse) {
        log.info("=== Amadeus API 응답 구조 분석 ===");
        log.info("응답 키들: {}", amadeusResponse.keySet());
        log.info("전체 응답: {}", amadeusResponse);
        
        // 첫 번째 항공편의 상세 구조 로깅
        @SuppressWarnings("unchecked")
        List<LinkedHashMap<String, Object>> offersData = (List<LinkedHashMap<String, Object>>) amadeusResponse.get("data");
        if (offersData != null && !offersData.isEmpty()) {
            LinkedHashMap<String, Object> firstOffer = offersData.get(0);
            log.info("=== 첫 번째 항공편 상세 구조 ===");
            log.info("첫 번째 항공편 키들: {}", firstOffer.keySet());
            log.info("첫 번째 항공편 전체: {}", firstOffer);
            
            // itineraries 구조 확인
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) firstOffer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                log.info("=== 첫 번째 itinerary 구조 ===");
                log.info("itinerary 키들: {}", firstItinerary.keySet());
                log.info("itinerary 전체: {}", firstItinerary);
                
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    log.info("=== 첫 번째 segment 구조 ===");
                    log.info("segment 키들: {}", firstSegment.keySet());
                    log.info("segment 전체: {}", firstSegment);
                    
                    // departure 구조 확인
                    LinkedHashMap<String, Object> departure = (LinkedHashMap<String, Object>) firstSegment.get("departure");
                    if (departure != null) {
                        log.info("=== departure 구조 ===");
                        log.info("departure 키들: {}", departure.keySet());
                        log.info("departure 전체: {}", departure);
                    }
                    
                    // arrival 구조 확인
                    LinkedHashMap<String, Object> arrival = (LinkedHashMap<String, Object>) firstSegment.get("arrival");
                    if (arrival != null) {
                        log.info("=== arrival 구조 ===");
                        log.info("arrival 키들: {}", arrival.keySet());
                        log.info("arrival 전체: {}", arrival);
                    }
                }
            }
        }
        
        try {
            // Amadeus API 응답 구조 파싱
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>) amadeusResponse.get("data");
            
            List<FlightSearchResponse.FlightOffer> flightOffers = new ArrayList<>();
            
            if (data != null && !data.isEmpty()) {
                for (LinkedHashMap<String, Object> offer : data) {
                    FlightSearchResponse.FlightOffer flightOffer = convertToFlightOffer(offer);
                    if (flightOffer != null) {
                        flightOffers.add(flightOffer);
                    }
                }
            }
            
            return FlightSearchResponse.builder()
                .apiProvider("AMADEUS")
                .status("SUCCESS")
                .message("항공편 검색 완료 - " + flightOffers.size() + "개 항공편 발견")
                .searchTimestamp(java.time.LocalDateTime.now())
                .flightOffers(flightOffers)
                .build();
                
        } catch (Exception e) {
            log.error("Amadeus API 응답 변환 실패: {}", e.getMessage());
            return FlightSearchResponse.builder()
                .apiProvider("AMADEUS")
                .status("ERROR")
                .message("응답 변환 중 오류 발생: " + e.getMessage())
                .searchTimestamp(java.time.LocalDateTime.now())
                .flightOffers(java.util.List.of())
                .build();
        }
    }
    
    /**
     * 개별 항공편 정보를 FlightOffer로 변환
     */
    private FlightSearchResponse.FlightOffer convertToFlightOffer(LinkedHashMap<String, Object> offer) {
        try {
            // 항공편 기본 정보
            String id = (String) offer.get("id");
            String airline = extractAirline(offer);
            String flightNumber = extractFlightNumber(offer);
            
            // 출발/도착 정보
            String originLocationCode = extractOriginLocationCode(offer);
            String destinationLocationCode = extractDestinationLocationCode(offer);
            String departureDate = extractDepartureDate(offer);
            String departureTime = extractDepartureTime(offer);
            String arrivalDate = extractArrivalDate(offer);
            String arrivalTime = extractArrivalTime(offer);
            
            // 가격 정보
            FlightSearchResponse.Price price = extractPrice(offer);
            
            return FlightSearchResponse.FlightOffer.builder()
                .id(id)
                .airline(airline)
                .flightNumber(flightNumber)
                .originLocationCode(originLocationCode)
                .destinationLocationCode(destinationLocationCode)
                .departureDate(departureDate)
                .departureTime(departureTime)
                .arrivalDate(arrivalDate)
                .arrivalTime(arrivalTime)
                .duration(extractDuration(offer))
                .cabinClass(extractCabinClass(offer))
                .price(price)
                .availableSeats(extractAvailableSeats(offer))
                .build();
                
        } catch (Exception e) {
            log.error("항공편 정보 변환 실패: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 폼 데이터 생성 (OAuth2 인증용)
     */
    private String buildFormData(Map<String, String> data) {
        StringBuilder formData = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (formData.length() > 0) {
                formData.append("&");
            }
            formData.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return formData.toString();
    }
    
    // 데이터 추출 메서드들
    private String extractAirline(LinkedHashMap<String, Object> offer) {
        try {
            // 실제 Amadeus API 응답 구조에 맞게 수정
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    return (String) firstSegment.get("carrierCode");
                }
            }
        } catch (Exception e) {
            log.warn("항공사 정보 추출 실패: {}", e.getMessage());
        }
        return "Unknown";
    }
    
    private String extractFlightNumber(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    return (String) firstSegment.get("number");
                }
            }
        } catch (Exception e) {
            log.warn("항공편 번호 추출 실패: {}", e.getMessage());
        }
        return "Unknown";
    }
    
    private String extractOriginLocationCode(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    LinkedHashMap<String, Object> departure = (LinkedHashMap<String, Object>) firstSegment.get("departure");
                    return departure != null ? (String) departure.get("iataCode") : null;
                }
            }
        } catch (Exception e) {
            log.warn("출발지 코드 추출 실패: {}", e.getMessage());
        }
        return null;
    }
    
    private String extractDestinationLocationCode(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> lastSegment = segments.get(segments.size() - 1);
                    LinkedHashMap<String, Object> arrival = (LinkedHashMap<String, Object>) lastSegment.get("arrival");
                    return arrival != null ? (String) arrival.get("iataCode") : null;
                }
            }
        } catch (Exception e) {
            log.warn("도착지 코드 추출 실패: {}", e.getMessage());
        }
        return null;
    }
    
    private String extractDepartureDate(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    LinkedHashMap<String, Object> departure = (LinkedHashMap<String, Object>) firstSegment.get("departure");
                    String dateTime = departure != null ? (String) departure.get("at") : null;
                    return dateTime != null ? dateTime.substring(0, 10) : null;
                }
            }
        } catch (Exception e) {
            log.warn("출발일 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractDepartureTime(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segments.get(0);
                    LinkedHashMap<String, Object> departure = (LinkedHashMap<String, Object>) firstSegment.get("departure");
                    String dateTime = departure != null ? (String) departure.get("at") : null;
                    return dateTime != null ? dateTime.substring(11, 16) : null;
                }
            }
        } catch (Exception e) {
            log.warn("출발시간 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractArrivalDate(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> lastSegment = segments.get(segments.size() - 1);
                    LinkedHashMap<String, Object> arrival = (LinkedHashMap<String, Object>) lastSegment.get("arrival");
                    String dateTime = arrival != null ? (String) arrival.get("at") : null;
                    return dateTime != null ? dateTime.substring(0, 10) : null;
                }
            }
        } catch (Exception e) {
            log.warn("도착일 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractArrivalTime(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segments = (List<LinkedHashMap<String, Object>>) firstItinerary.get("segments");
                if (segments != null && !segments.isEmpty()) {
                    LinkedHashMap<String, Object> lastSegment = segments.get(segments.size() - 1);
                    LinkedHashMap<String, Object> arrival = (LinkedHashMap<String, Object>) lastSegment.get("arrival");
                    String dateTime = arrival != null ? (String) arrival.get("at") : null;
                    return dateTime != null ? dateTime.substring(11, 16) : null;
                }
            }
        } catch (Exception e) {
            log.warn("도착시간 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractDuration(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itineraries = (List<LinkedHashMap<String, Object>>) offer.get("itineraries");
            if (itineraries != null && !itineraries.isEmpty()) {
                LinkedHashMap<String, Object> firstItinerary = itineraries.get(0);
                return (String) firstItinerary.get("duration");
            }
        } catch (Exception e) {
            log.warn("소요시간 추출 실패: {}", e.getMessage());
        }
        return null;
    }
    
    private String extractCabinClass(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> travelers = (List<LinkedHashMap<String, Object>>) offer.get("travelerPricings");
            if (travelers != null && !travelers.isEmpty()) {
                LinkedHashMap<String, Object> firstTraveler = travelers.get(0);
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> fareDetails = (List<LinkedHashMap<String, Object>>) firstTraveler.get("fareDetailsBySegment");
                if (fareDetails != null && !fareDetails.isEmpty()) {
                    LinkedHashMap<String, Object> firstFare = fareDetails.get(0);
                    return (String) firstFare.get("cabin");
                }
            }
        } catch (Exception e) {
            log.warn("좌석 등급 추출 실패: {}", e.getMessage());
        }
        return "ECONOMY";
    }
    
    private FlightSearchResponse.Price extractPrice(LinkedHashMap<String, Object> offer) {
        try {
            // 실제 Amadeus API 응답 구조에 맞게 수정
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> travelerPricings = (List<LinkedHashMap<String, Object>>) offer.get("travelerPricings");
            if (travelerPricings != null && !travelerPricings.isEmpty()) {
                LinkedHashMap<String, Object> firstTraveler = travelerPricings.get(0);
                LinkedHashMap<String, Object> price = (LinkedHashMap<String, Object>) firstTraveler.get("price");
                if (price != null) {
                    String currency = (String) price.get("currency");
                    Double total = Double.valueOf(price.get("total").toString());
                    
                    return FlightSearchResponse.Price.builder()
                        .currency(currency)
                        .total(total)
                        .base(total * 0.8) // 예시: 기본 요금은 총 요금의 80%
                        .taxes(total * 0.2) // 예시: 세금은 총 요금의 20%
                        .build();
                }
            }
        } catch (Exception e) {
            log.warn("가격 정보 추출 실패: {}", e.getMessage());
        }
        return FlightSearchResponse.Price.builder()
            .currency("USD")
            .total(0.0)
            .base(0.0)
            .taxes(0.0)
            .build();
    }
    
    private Integer extractAvailableSeats(LinkedHashMap<String, Object> offer) {
        try {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> segments = (LinkedHashMap<String, Object>) offer.get("itineraries");
            if (segments != null) {
                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> segmentsList = (List<LinkedHashMap<String, Object>>) segments.get("segments");
                if (segmentsList != null && !segmentsList.isEmpty()) {
                    LinkedHashMap<String, Object> firstSegment = segmentsList.get(0);
                    return (Integer) firstSegment.get("numberOfBookableSeats");
                }
            }
        } catch (Exception e) {
            log.warn("가용 좌석 수 추출 실패: {}", e.getMessage());
        }
        return 9; // 기본값
    }
} 