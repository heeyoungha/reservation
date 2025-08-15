package com.example.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class FlightSearchRequest {
    
    @NotBlank(message = "출발지 코드는 필수입니다")
    @Size(min = 3, max = 3, message = "공항 코드는 3자리여야 합니다")
    @Pattern(regexp = "^[A-Z]{3}$", message = "공항 코드는 3자리 대문자 알파벳이어야 합니다")
    String originLocationCode;
    
    @NotBlank(message = "도착지 코드는 필수입니다")
    @Size(min = 3, max = 3, message = "공항 코드는 3자리여야 합니다")
    @Pattern(regexp = "^[A-Z]{3}$", message = "공항 코드는 3자리 대문자 알파벳이어야 합니다")
    String destinationLocationCode;
    
    @NotNull(message = "출발일은 필수입니다")
    @Future(message = "출발일은 오늘 이후여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    
    @Min(value = 1, message = "성인 승객은 최소 1명입니다")
    @Max(value = 9, message = "성인 승객은 최대 9명입니다")
    @Builder.Default
    Integer adults = 1;
    
    @Min(value = 0, message = "아동 승객은 0 이상이어야 합니다")
    @Max(value = 8, message = "아동 승객은 최대 8명입니다")
    @Builder.Default
    Integer children = 0;
    
    @Min(value = 0, message = "유아 승객은 0 이상이어야 합니다")
    @Max(value = 2, message = "유아 승객은 최대 2명입니다")
    @Builder.Default
    Integer infants = 0;
    
    @NotBlank(message = "API 제공자는 필수입니다")
    String apiProvider;
    
    // 커스텀 검증 메서드
    @AssertTrue(message = "출발지와 도착지는 달라야 합니다")
    private boolean isValidRoute() {
        return originLocationCode == null || destinationLocationCode == null || 
               !originLocationCode.equals(destinationLocationCode);
    }
    
    @AssertTrue(message = "왕복 항공편의 경우 복귀일은 출발일 이후여야 합니다")
    private boolean isValidReturnDate() {
        return returnDate == null || departureDate == null || 
               returnDate.isAfter(departureDate);
    }
    
    @AssertTrue(message = "총 승객 수는 9명을 초과할 수 없습니다")
    private boolean isValidTotalPassengers() {
        int total = (adults != null ? adults : 0) + 
                   (children != null ? children : 0) + 
                   (infants != null ? infants : 0);
        return total <= 9;
    }
    
    // 편의 메서드들
    public boolean isRoundTrip() {
        return returnDate != null;
    }
    
    public int getTotalPassengers() {
        return (adults != null ? adults : 0) + 
               (children != null ? children : 0) + 
               (infants != null ? infants : 0);
    }
}