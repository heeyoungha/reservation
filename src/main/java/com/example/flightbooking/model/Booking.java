package com.example.flightbooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_reference", columnList = "bookingReference"),
        @Index(name = "idx_passenger_email", columnList = "passengerEmail"),
        @Index(name = "idx_booking_status", columnList = "status")
})
@Data  // @Value 대신 @Data 사용 (JPA에는 setter 필요)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
    @NotBlank(message = "예약 참조번호는 필수입니다")
    private String bookingReference;

    @Column(name = "flight_number", nullable = false, length = 10)
    @NotBlank(message = "항공편명은 필수입니다")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "유효한 항공편명 형식이 아닙니다")
    private String flightNumber;

    @Column(name = "origin_location_code", nullable = false, length = 3)
    @NotBlank(message = "출발지 코드는 필수입니다")
    @Size(min = 3, max = 3, message = "공항 코드는 3자리여야 합니다")
    private String originLocationCode;

    @Column(name = "destination_location_code", nullable = false, length = 3)
    @NotBlank(message = "도착지 코드는 필수입니다")
    @Size(min = 3, max = 3, message = "공항 코드는 3자리여야 합니다")
    private String destinationLocationCode;

    @Column(name = "departure_date", nullable = false)
    @NotNull(message = "출발일은 필수입니다")
    private LocalDate departureDate;

    @Column(name = "departure_time", nullable = false)
    @NotNull(message = "출발 시간은 필수입니다")
    private LocalTime departureTime;  // String → LocalTime으로 변경

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "return_time")
    private LocalTime returnTime;  // String → LocalTime으로 변경

    @Column(name = "passenger_name", nullable = false, length = 100)
    @NotBlank(message = "승객명은 필수입니다")
    @Size(max = 100, message = "승객명은 100자를 초과할 수 없습니다")
    private String passengerName;

    @Column(name = "passenger_email", nullable = false, length = 255)
    @Email(message = "유효한 이메일 주소를 입력해주세요")
    @NotBlank(message = "승객 이메일은 필수입니다")
    private String passengerEmail;

    @Column(name = "passenger_phone", nullable = false, length = 20)
    @Pattern(regexp = "^[0-9-+()\\s]+$", message = "유효한 전화번호 형식이 아닙니다")
    @NotBlank(message = "승객 전화번호는 필수입니다")
    private String passengerPhone;

    @Column(name = "api_provider", nullable = false, length = 50)
    @NotBlank(message = "API 제공자는 필수입니다")
    private String apiProvider;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "예약 상태는 필수입니다")
    @Pattern(regexp = "^(PENDING|CONFIRMED|CANCELLED|FAILED)$",
            message = "예약 상태는 PENDING, CONFIRMED, CANCELLED, FAILED 중 하나여야 합니다")
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "booking_timestamp", nullable = false)
    @CreatedDate
    private LocalDateTime bookingTimestamp;

    @Column(name = "booking_response", columnDefinition = "TEXT")
    private String bookingResponse;

    @Column(name = "total_amount")
    private java.math.BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "통화 코드는 3자리여야 합니다")
    private String currency;

    // 예약 참조번호 자동 생성
    @PrePersist
    protected void generateBookingReference() {
        if (bookingReference == null || bookingReference.isEmpty()) {
            bookingReference = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // 비즈니스 로직 메서드들
    public boolean isRoundTrip() {
        return returnDate != null;
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean canBeCancelled() {
        return isConfirmed() || isPending();
    }

    public boolean isInternationalFlight() {
        return originLocationCode != null && destinationLocationCode != null &&
                originLocationCode.length() >= 2 && destinationLocationCode.length() >= 2 &&
                !originLocationCode.substring(0, 2).equals(destinationLocationCode.substring(0, 2));
    }

    public boolean isPastDeparture() {
        if (departureDate == null) return false;
        return departureDate.isBefore(LocalDate.now()) ||
                (departureDate.equals(LocalDate.now()) && departureTime != null &&
                        departureTime.isBefore(LocalTime.now()));
    }
}