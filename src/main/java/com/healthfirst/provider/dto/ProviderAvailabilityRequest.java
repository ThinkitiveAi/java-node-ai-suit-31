package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.ProviderAvailability;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAvailabilityRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    private ProviderAvailability.RecurrenceType recurrenceType;

    private Set<DayOfWeek> recurrenceDays;

    private LocalDateTime recurrenceEndDate;

    @NotNull(message = "Slot duration is required")
    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 480, message = "Slot duration cannot exceed 8 hours")
    private Integer slotDurationMinutes;

    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal price;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency;

    private String location;

    private String appointmentType;

    private String specialRequirements;

    @Builder.Default
    private ProviderAvailability.AvailabilityStatus status = ProviderAvailability.AvailabilityStatus.ACTIVE;

    private String notes;
} 