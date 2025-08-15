package com.example.flightbooking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World! Flight Booking System is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
