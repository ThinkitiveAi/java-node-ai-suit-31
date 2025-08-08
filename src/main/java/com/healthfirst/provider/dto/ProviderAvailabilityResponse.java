package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.ProviderAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAvailabilityResponse {

    private Long id;
    private Long providerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timezone;
    private ProviderAvailability.RecurrenceType recurrenceType;
    private Set<DayOfWeek> recurrenceDays;
    private LocalDateTime recurrenceEndDate;
    private Integer slotDurationMinutes;
    private BigDecimal price;
    private String currency;
    private String location;
    private String appointmentType;
    private String specialRequirements;
    private ProviderAvailability.AvailabilityStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Slot statistics
    private Long totalSlots;
    private Long availableSlots;
    private Long bookedSlots;
    private Long cancelledSlots;
    
    // Detailed slot information
    private List<AppointmentSlotResponse> appointmentSlots;
} 