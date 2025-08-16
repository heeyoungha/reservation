package com.example.flightsearch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "amadeus")
@Getter
@Setter
public class AmadeusConfig {
    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String authUrl;
    private int timeout;
    private int maxRetries;
    
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