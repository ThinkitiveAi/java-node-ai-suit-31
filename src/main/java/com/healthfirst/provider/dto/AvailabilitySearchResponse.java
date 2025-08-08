package com.healthfirst.provider.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilitySearchResponse {
    private List<AvailableSlot> availableSlots;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableSlot {
        private UUID slotId;
        private UUID providerId;
        private String providerName;
        private String specialization;
        private String clinicAddress;
        private LocalDateTime slotStartTime;
        private LocalDateTime slotEndTime;
        private String timezone;
        private BigDecimal price;
        private String appointmentType;
        private String specialRequirements;
        private long durationMinutes;
        private String providerPhone;
        private String providerEmail;
    }
} 