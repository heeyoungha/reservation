package com.example.flightsearch.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * Amadeus API 호출을 위한 HTTP 클라이언트 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final AmadeusConfig amadeusConfig;
    
    /**
     * Amadeus API용 WebClient
     */
    @Bean("amadeusWebClient")
    public WebClient amadeusWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, amadeusConfig.getTimeout())
            .responseTimeout(Duration.ofMillis(amadeusConfig.getTimeout()))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(amadeusConfig.getTimeout(), TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(amadeusConfig.getTimeout(), TimeUnit.MILLISECONDS))
            );
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(amadeusConfig.getBaseUrl())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }
    
    /**
     * 일반적인 HTTP 요청용 WebClient
     */
    @Bean("defaultWebClient")
    public WebClient defaultWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
            );
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build();
    }
} 