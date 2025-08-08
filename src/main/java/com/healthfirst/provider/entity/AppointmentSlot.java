package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_availability_id", nullable = false)
    private ProviderAvailability providerAvailability;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "timezone", nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlotStatus status;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "location")
    private String location;

    @Column(name = "appointment_type")
    private String appointmentType;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "booking_notes", columnDefinition = "TEXT")
    private String bookingNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SlotStatus {
        AVAILABLE, BOOKED, CANCELLED, COMPLETED, NO_SHOW
    }
} 