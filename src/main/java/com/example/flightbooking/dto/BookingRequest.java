package com.example.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Value
@Builder
public class BookingRequest {

    @NotBlank(message = "항공편명은 필수입니다")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "유효한 항공편명 형식이 아닙니다")
    String flightNumber;

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

    @NotNull(message = "출발 시간은 필수입니다")
    @JsonFormat(pattern = "HH:mm")
    LocalTime departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;

    @JsonFormat(pattern = "HH:mm")
    LocalTime returnTime;

    @NotBlank(message = "승객명은 필수입니다")
    @Size(min = 2, max = 100, message = "승객명은 2자 이상 100자 이하여야 합니다")
    String passengerName;

    @NotBlank(message = "승객 이메일은 필수입니다")
    @Email(message = "유효한 이메일 주소를 입력해주세요")
    String passengerEmail;

    @NotBlank(message = "승객 전화번호는 필수입니다")
    @Pattern(regexp = "^[0-9-+()\\s]+$", message = "유효한 전화번호 형식이 아닙니다")
    String passengerPhone;

    @NotBlank(message = "API 제공자는 필수입니다")
    String apiProvider;

    @DecimalMin(value = "0.01", message = "예약 금액은 0보다 커야 합니다")
    @Digits(integer = 10, fraction = 2, message = "올바른 금액 형식이 아닙니다")
    BigDecimal totalAmount;

    @Size(min = 3, max = 3, message = "통화 코드는 3자리여야 합니다")
    @Pattern(regexp = "^[A-Z]{3}$", message = "통화 코드는 3자리 대문자 알파벳이어야 합니다")
    String currency;

    // 커스텀 검증 메서드들
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

    @AssertTrue(message = "왕복 항공편의 경우 복귀 시간은 필수입니다")
    private boolean isValidReturnTime() {
        return returnDate == null || returnTime != null;
    }

    // 편의 메서드들
    public boolean isRoundTrip() {
        return returnDate != null;
    }
}