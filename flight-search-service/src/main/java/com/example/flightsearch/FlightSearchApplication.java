package com.example.flightsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlightSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightSearchApplication.class, args);
    }

} 