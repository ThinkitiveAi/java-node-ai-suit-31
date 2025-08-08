package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityResponse;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderAvailabilityServiceTest {

    @Mock
    private ProviderAvailabilityRepository availabilityRepository;

    @Mock
    private AppointmentSlotRepository slotRepository;

    @InjectMocks
    private ProviderAvailabilityService availabilityService;

    private ProviderAvailabilityRequest validRequest;
    private ProviderAvailability mockAvailability;
    private List<AppointmentSlot> mockSlots;

    @BeforeEach
    void setUp() {
        validRequest = ProviderAvailabilityRequest.builder()
                .providerId(1L)
                .startTime(LocalDateTime.of(2024, 1, 15, 9, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 17, 0))
                .timezone("America/New_York")
                .slotDurationMinutes(30)
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .location("New York Medical Center")
                .appointmentType("CONSULTATION")
                .status(ProviderAvailability.AvailabilityStatus.ACTIVE)
                .build();

        mockAvailability = ProviderAvailability.builder()
                .id(1L)
                .providerId(1L)
                .startTime(LocalDateTime.of(2024, 1, 15, 14, 0)) // UTC time
                .endTime(LocalDateTime.of(2024, 1, 15, 22, 0)) // UTC time
                .timezone("America/New_York")
                .recurrenceType(ProviderAvailability.RecurrenceType.NONE)
                .slotDurationMinutes(30)
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .location("New York Medical Center")
                .appointmentType("CONSULTATION")
                .status(ProviderAvailability.AvailabilityStatus.ACTIVE)
                .build();

        mockSlots = Arrays.asList(
                AppointmentSlot.builder()
                        .id(1L)
                        .providerId(1L)
                        .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                        .endTime(LocalDateTime.of(2024, 1, 15, 14, 30))
                        .status(AppointmentSlot.SlotStatus.AVAILABLE)
                        .price(new BigDecimal("100.00"))
                        .build(),
                AppointmentSlot.builder()
                        .id(2L)
                        .providerId(1L)
                        .startTime(LocalDateTime.of(2024, 1, 15, 14, 30))
                        .endTime(LocalDateTime.of(2024, 1, 15, 15, 0))
                        .status(AppointmentSlot.SlotStatus.BOOKED)
                        .price(new BigDecimal("100.00"))
                        .build()
        );
    }

    @Test
    void createAvailability_Success() {
        // Arrange
        when(availabilityRepository.existsOverlappingAvailability(anyLong(), any(), any(), any()))
                .thenReturn(false);
        when(availabilityRepository.save(any(ProviderAvailability.class)))
                .thenReturn(mockAvailability);
        when(slotRepository.saveAll(anyList()))
                .thenReturn(mockSlots);
        when(slotRepository.findByProviderAvailabilityId(anyLong()))
                .thenReturn(mockSlots);

        // Act
        ProviderAvailabilityResponse response = availabilityService.createAvailability(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getProviderId());
        assertEquals(2L, response.getTotalSlots());
        assertEquals(1L, response.getAvailableSlots());
        assertEquals(1L, response.getBookedSlots());
        verify(availabilityRepository).save(any(ProviderAvailability.class));
        verify(slotRepository).saveAll(anyList());
    }

    @Test
    void createAvailability_InvalidTimeRange_ThrowsException() {
        // Arrange
        validRequest.setStartTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        validRequest.setEndTime(LocalDateTime.of(2024, 1, 15, 9, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            availabilityService.createAvailability(validRequest);
        });
    }

    @Test
    void createAvailability_OverlappingAvailability_ThrowsException() {
        // Arrange
        when(availabilityRepository.existsOverlappingAvailability(anyLong(), any(), any(), any()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            availabilityService.createAvailability(validRequest);
        });
    }

    @Test
    void createAvailability_WithRecurrence_Success() {
        // Arrange
        validRequest.setRecurrenceType(ProviderAvailability.RecurrenceType.WEEKLY);
        validRequest.setRecurrenceDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        validRequest.setRecurrenceEndDate(LocalDateTime.of(2024, 2, 15, 17, 0));

        ProviderAvailability mockRecurringAvailability = ProviderAvailability.builder()
                .id(1L)
                .providerId(1L)
                .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 22, 0))
                .timezone("America/New_York")
                .recurrenceType(ProviderAvailability.RecurrenceType.WEEKLY)
                .recurrenceDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
                .recurrenceEndDate(LocalDateTime.of(2024, 2, 15, 22, 0))
                .slotDurationMinutes(30)
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .location("New York Medical Center")
                .appointmentType("CONSULTATION")
                .status(ProviderAvailability.AvailabilityStatus.ACTIVE)
                .build();

        when(availabilityRepository.existsOverlappingAvailability(anyLong(), any(), any(), any()))
                .thenReturn(false);
        when(availabilityRepository.save(any(ProviderAvailability.class)))
                .thenReturn(mockRecurringAvailability);
        when(slotRepository.saveAll(anyList()))
                .thenReturn(mockSlots);
        when(slotRepository.findByProviderAvailabilityId(anyLong()))
                .thenReturn(mockSlots);

        // Act
        ProviderAvailabilityResponse response = availabilityService.createAvailability(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(ProviderAvailability.RecurrenceType.WEEKLY, response.getRecurrenceType());
        assertNotNull(response.getRecurrenceDays());
        assertEquals(3, response.getRecurrenceDays().size());
    }

    @Test
    void getProviderAvailability_Success() {
        // Arrange
        when(availabilityRepository.findByProviderIdAndStatus(anyLong(), any()))
                .thenReturn(Arrays.asList(mockAvailability));
        when(slotRepository.findByProviderAvailabilityId(anyLong()))
                .thenReturn(mockSlots);

        // Act
        ProviderAvailabilityResponse response = availabilityService.getProviderAvailability(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getProviderId());
        assertEquals(2L, response.getTotalSlots());
    }

    @Test
    void getProviderAvailability_NoAvailability_ThrowsException() {
        // Arrange
        when(availabilityRepository.findByProviderIdAndStatus(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            availabilityService.getProviderAvailability(1L);
        });
    }

    @Test
    void updateSlot_Success() {
        // Arrange
        AppointmentSlot mockSlot = AppointmentSlot.builder()
                .id(1L)
                .providerId(1L)
                .status(AppointmentSlot.SlotStatus.AVAILABLE)
                .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 14, 30))
                .build();

        AppointmentSlotResponse updateRequest = AppointmentSlotResponse.builder()
                .price(new BigDecimal("150.00"))
                .status(AppointmentSlot.SlotStatus.CANCELLED)
                .build();

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(mockSlot));
        when(slotRepository.save(any(AppointmentSlot.class)))
                .thenReturn(mockSlot);

        // Act
        AppointmentSlotResponse response = availabilityService.updateSlot(1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(slotRepository).save(any(AppointmentSlot.class));
    }

    @Test
    void updateSlot_BookedSlot_ThrowsException() {
        // Arrange
        AppointmentSlot mockSlot = AppointmentSlot.builder()
                .id(1L)
                .status(AppointmentSlot.SlotStatus.BOOKED)
                .build();

        AppointmentSlotResponse updateRequest = AppointmentSlotResponse.builder()
                .price(new BigDecimal("150.00"))
                .build();

        when(slotRepository.findById(1L))
                .thenReturn(Optional.of(mockSlot));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            availabilityService.updateSlot(1L, updateRequest);
        });
    }

    @Test
    void updateSlot_SlotNotFound_ThrowsException() {
        // Arrange
        when(slotRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            availabilityService.updateSlot(1L, new AppointmentSlotResponse());
        });
    }

    @Test
    void deleteAvailability_Success() {
        // Arrange
        when(availabilityRepository.findById(1L))
                .thenReturn(Optional.of(mockAvailability));
        when(slotRepository.countBookedSlotsByAvailabilityId(1L))
                .thenReturn(0L);

        // Act
        assertDoesNotThrow(() -> {
            availabilityService.deleteAvailability(1L, false);
        });

        // Assert
        verify(availabilityRepository).save(any(ProviderAvailability.class));
    }

    @Test
    void deleteAvailability_WithBookedSlots_ThrowsException() {
        // Arrange
        when(availabilityRepository.findById(1L))
                .thenReturn(Optional.of(mockAvailability));
        when(slotRepository.countBookedSlotsByAvailabilityId(1L))
                .thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            availabilityService.deleteAvailability(1L, false);
        });
    }

    @Test
    void deleteAvailability_RecurringSlots_Success() {
        // Arrange
        mockAvailability.setRecurrenceType(ProviderAvailability.RecurrenceType.WEEKLY);
        when(availabilityRepository.findById(1L))
                .thenReturn(Optional.of(mockAvailability));
        when(slotRepository.countBookedSlotsByAvailabilityId(1L))
                .thenReturn(0L);
        when(slotRepository.findByProviderAvailabilityId(1L))
                .thenReturn(mockSlots);

        // Act
        assertDoesNotThrow(() -> {
            availabilityService.deleteAvailability(1L, true);
        });

        // Assert
        verify(slotRepository).deleteAll(mockSlots);
        verify(availabilityRepository).delete(mockAvailability);
    }

    @Test
    void searchAvailability_Success() {
        // Arrange
        AvailabilitySearchRequest searchRequest = AvailabilitySearchRequest.builder()
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .location("New York")
                .appointmentType("CONSULTATION")
                .timezone("America/New_York")
                .build();

        when(slotRepository.findAvailableSlotsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockSlots);

        // Act
        List<AppointmentSlotResponse> response = availabilityService.searchAvailability(searchRequest);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(slotRepository).findAvailableSlotsByFilters(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchAvailability_WithFilters_Success() {
        // Arrange
        AvailabilitySearchRequest searchRequest = AvailabilitySearchRequest.builder()
                .startDate(LocalDate.of(2024, 1, 15))
                .providerId(1L)
                .maxPrice(200)
                .slotDurationMinutes(30)
                .build();

        when(slotRepository.findAvailableSlotsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockSlots);

        // Act
        List<AppointmentSlotResponse> response = availabilityService.searchAvailability(searchRequest);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void timezoneConversion_UTC_Storage() {
        // Arrange
        validRequest.setStartTime(LocalDateTime.of(2024, 1, 15, 9, 0)); // 9 AM EST
        validRequest.setEndTime(LocalDateTime.of(2024, 1, 15, 17, 0)); // 5 PM EST
        validRequest.setTimezone("America/New_York");

        when(availabilityRepository.existsOverlappingAvailability(anyLong(), any(), any(), any()))
                .thenReturn(false);
        when(availabilityRepository.save(any(ProviderAvailability.class)))
                .thenReturn(mockAvailability);
        when(slotRepository.saveAll(anyList()))
                .thenReturn(mockSlots);
        when(slotRepository.findByProviderAvailabilityId(anyLong()))
                .thenReturn(mockSlots);

        // Act
        ProviderAvailabilityResponse response = availabilityService.createAvailability(validRequest);

        // Assert
        assertNotNull(response);
        // The stored times should be in UTC (EST + 5 hours during winter)
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), response.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 15, 22, 0), response.getEndTime());
    }
} 