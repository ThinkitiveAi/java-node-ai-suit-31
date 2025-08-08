package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, Long> {

    List<ProviderAvailability> findByProviderIdAndStatus(Long providerId, ProviderAvailability.AvailabilityStatus status);

    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId AND pa.status = :status AND " +
           "((pa.recurrenceType = 'NONE' AND pa.startTime >= :startDate AND pa.endTime <= :endDate) OR " +
           "(pa.recurrenceType != 'NONE' AND pa.recurrenceEndDate >= :startDate))")
    List<ProviderAvailability> findByProviderIdAndStatusAndDateRange(
            @Param("providerId") Long providerId,
            @Param("status") ProviderAvailability.AvailabilityStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.location LIKE %:location% AND pa.status = 'ACTIVE'")
    List<ProviderAvailability> findByLocationContaining(@Param("location") String location);

    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.appointmentType = :appointmentType AND pa.status = 'ACTIVE'")
    List<ProviderAvailability> findByAppointmentType(@Param("appointmentType") String appointmentType);

    @Query("SELECT pa FROM ProviderAvailability pa WHERE pa.providerId = :providerId AND pa.startTime >= :startTime AND pa.endTime <= :endTime")
    List<ProviderAvailability> findOverlappingAvailability(
            @Param("providerId") Long providerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(pa) > 0 FROM ProviderAvailability pa WHERE pa.providerId = :providerId AND pa.id != :excludeId AND " +
           "pa.status = 'ACTIVE' AND ((pa.startTime < :endTime AND pa.endTime > :startTime) OR " +
           "(pa.recurrenceType != 'NONE' AND pa.recurrenceEndDate >= :startTime))")
    boolean existsOverlappingAvailability(
            @Param("providerId") Long providerId,
            @Param("excludeId") Long excludeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
} 