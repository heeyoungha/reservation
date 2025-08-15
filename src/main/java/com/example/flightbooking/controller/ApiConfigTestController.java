package com.example.flightbooking.controller;

import com.example.flightbooking.config.AmadeusConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * API 설정 테스트를 위한 임시 컨트롤러
 * 실제 프로덕션에서는 제거하거나 적절한 테스트 코드로 대체
 */
@RestController
@RequestMapping("/api/config-test")
@RequiredArgsConstructor
@Slf4j
public class ApiConfigTestController {
    
    private final AmadeusConfig amadeusConfig;
    
    @Qualifier("amadeusWebClient")
    private final WebClient amadeusWebClient;
    
    @Qualifier("defaultWebClient")
    private final WebClient defaultWebClient;
    
    /**
     * Amadeus 설정 확인
     */
    @GetMapping("/amadeus")
    public String testAmadeusConfig() {
        log.info("Amadeus 설정 테스트");
        
        StringBuilder result = new StringBuilder();
        result.append("✅ Amadeus 설정 확인\n");
        result.append("Base URL: ").append(amadeusConfig.getBaseUrl()).append("\n");
        result.append("Auth URL: ").append(amadeusConfig.getAuthUrl()).append("\n");
        result.append("Flight Search URL: ").append(amadeusConfig.getFlightOffersSearchUrl()).append("\n");
        result.append("Timeout: ").append(amadeusConfig.getTimeout()).append("ms\n");
        result.append("Max Retries: ").append(amadeusConfig.getMaxRetries()).append("\n");
        result.append("Client ID: ").append(amadeusConfig.getClientId() != null ? "설정됨" : "설정되지 않음");
        
        return result.toString();
    }
    
    /**
     * WebClient 설정 확인
     */
    @GetMapping("/webclient")
    public String testWebClientConfig() {
        log.info("WebClient 설정 테스트");
        
        StringBuilder result = new StringBuilder();
        result.append("✅ WebClient 설정 확인\n");
        result.append("Amadeus WebClient: ").append(amadeusWebClient != null ? "생성됨" : "생성되지 않음").append("\n");
        result.append("Default WebClient: ").append(defaultWebClient != null ? "생성됨" : "생성되지 않음");
        
        return result.toString();
    }
} 