package com.example.flightbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_searches")
@Data  // @Value 대신 @Data 사용 (JPA에는 setter 필요)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FlightSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "origin_location_code", nullable = false, length = 3)
    @NotBlank(message = "출발지 코드는 필수입니다")
    private String originLocationCode;
    
    @Column(name = "destination_location_code", nullable = false, length = 3)
    @NotBlank(message = "도착지 코드는 필수입니다")
    private String destinationLocationCode;
    
    @Column(name = "departure_date", nullable = false)
    @NotNull(message = "출발일은 필수입니다")
    private LocalDate departureDate;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Column(nullable = false)
    @Min(value = 1, message = "성인 승객은 최소 1명입니다")
    @Max(value = 9, message = "성인 승객은 최대 9명입니다")
    @Builder.Default
    private Integer adults = 1;
    
    @Column
    @Min(value = 0, message = "아동 승객은 0 이상이어야 합니다")
    @Max(value = 8, message = "아동 승객은 최대 8명입니다")
    @Builder.Default
    private Integer children = 0;
    
    @Column
    @Min(value = 0, message = "유아 승객은 0 이상이어야 합니다")
    @Max(value = 2, message = "유아 승객은 최대 2명입니다")
    @Builder.Default
    private Integer infants = 0;
    
    @Column(name = "api_provider", nullable = false)
    @NotBlank(message = "API 제공자는 필수입니다")
    private String apiProvider;
    
    @Column(name = "search_timestamp", nullable = false)
    @CreatedDate
    private LocalDateTime searchTimestamp;
    
    @Column(name = "search_response", columnDefinition = "TEXT")
    private String searchResponse;
    
    // 비즈니스 로직 메서드들
    public boolean isRoundTrip() {
        return returnDate != null;
    }
    
    public int getTotalPassengers() {
        return (adults != null ? adults : 0) + 
               (children != null ? children : 0) + 
               (infants != null ? infants : 0);
    }
    
    public boolean isValidSearch() {
        return originLocationCode != null && 
               destinationLocationCode != null &&
               !originLocationCode.equals(destinationLocationCode) &&
               departureDate != null &&
               departureDate.isAfter(LocalDate.now());
    }
    
    public boolean isInternationalFlight() {
        return originLocationCode != null && destinationLocationCode != null &&
               originLocationCode.length() >= 2 && destinationLocationCode.length() >= 2 &&
               !originLocationCode.substring(0, 2).equals(destinationLocationCode.substring(0, 2));
    }
}