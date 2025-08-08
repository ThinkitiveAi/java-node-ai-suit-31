package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByProviderIdAndStatus(Long providerId, AppointmentSlot.SlotStatus status);

    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId AND as.status = 'AVAILABLE' AND " +
           "as.startTime >= :startTime AND as.endTime <= :endTime")
    List<AppointmentSlot> findAvailableSlotsByProviderAndDateRange(
            @Param("providerId") Long providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT as FROM AppointmentSlot as WHERE as.status = 'AVAILABLE' AND as.startTime >= :startTime AND " +
           "as.endTime <= :endTime AND (:location IS NULL OR as.location LIKE %:location%) AND " +
           "(:appointmentType IS NULL OR as.appointmentType = :appointmentType) AND " +
           "(:providerId IS NULL OR as.providerId = :providerId) AND " +
           "(:maxPrice IS NULL OR as.price <= :maxPrice) AND " +
           "(:slotDurationMinutes IS NULL OR (as.endTime - as.startTime) = :slotDurationMinutes)")
    List<AppointmentSlot> findAvailableSlotsByFilters(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("location") String location,
            @Param("appointmentType") String appointmentType,
            @Param("providerId") Long providerId,
            @Param("maxPrice") Integer maxPrice,
            @Param("slotDurationMinutes") Integer slotDurationMinutes
    );

    @Query("SELECT COUNT(as) FROM AppointmentSlot as WHERE as.providerAvailability.id = :availabilityId AND as.status = 'BOOKED'")
    long countBookedSlotsByAvailabilityId(@Param("availabilityId") Long availabilityId);

    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerAvailability.id = :availabilityId")
    List<AppointmentSlot> findByProviderAvailabilityId(@Param("availabilityId") Long availabilityId);

    @Query("SELECT as FROM AppointmentSlot as WHERE as.providerId = :providerId AND as.startTime >= :startTime AND " +
           "as.endTime <= :endTime AND as.status = 'BOOKED'")
    List<AppointmentSlot> findBookedSlotsByProviderAndDateRange(
            @Param("providerId") Long providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
} 