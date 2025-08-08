package com.healthfirst.provider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySearchRequest {

    private String location;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String appointmentType;
    
    private Long providerId;
    
    private Integer maxPrice;
    
    private String timezone;
    
    private Integer slotDurationMinutes;
} 