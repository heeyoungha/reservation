package com.example.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello World! Flight Booking System is running!";
    }
    
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
    
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html"; // 정적 파일로 리다이렉트
    }
}
