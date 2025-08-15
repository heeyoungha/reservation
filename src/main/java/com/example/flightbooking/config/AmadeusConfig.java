package com.example.flightbooking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Amadeus API 설정
 * application.yml에서 amadeus.* 속성들을 바인딩
 */
@Configuration
@ConfigurationProperties(prefix = "amadeus")
@Data
public class AmadeusConfig {
    
    private String clientId;
    private String clientSecret;
    private String baseUrl = "https://test.api.amadeus.com/v2";
    private String authUrl = "https://test.api.amadeus.com/v1/security/oauth2/token";
    private int timeout = 10000; // 10초
    private int maxRetries = 3;
    
    // API 엔드포인트들
    public String getFlightOffersSearchUrl() {
        return baseUrl + "/shopping/flight-offers";
    }
    
    public String getFlightOfferPricingUrl() {
        return baseUrl + "/shopping/flight-offers/pricing";
    }
    
    public String getFlightOrdersUrl() {
        return baseUrl + "/booking/flight-orders";
    }
} 