package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.AppointmentSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotResponse {

    private Long id;
    private Long providerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timezone;
    private AppointmentSlot.SlotStatus status;
    private BigDecimal price;
    private String currency;
    private String location;
    private String appointmentType;
    private String specialRequirements;
    private Long patientId;
    private String bookingNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 