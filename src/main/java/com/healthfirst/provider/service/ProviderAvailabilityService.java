package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityResponse;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.repository.AppointmentSlotRepository;
import com.healthfirst.provider.repository.ProviderAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderAvailabilityService {

    private final ProviderAvailabilityRepository availabilityRepository;
    private final AppointmentSlotRepository slotRepository;

    @Transactional
    public ProviderAvailabilityResponse createAvailability(ProviderAvailabilityRequest request) {
        log.info("Creating availability for provider: {}", request.getProviderId());

        // Validate time range
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Check for overlapping availability
        if (availabilityRepository.existsOverlappingAvailability(
                request.getProviderId(), null, request.getStartTime(), request.getEndTime())) {
            throw new IllegalArgumentException("Availability overlaps with existing schedule");
        }

        // Convert to UTC for storage
        ZoneId providerZone = ZoneId.of(request.getTimezone());
        LocalDateTime utcStartTime = request.getStartTime().atZone(providerZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime utcEndTime = request.getEndTime().atZone(providerZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        ProviderAvailability availability = ProviderAvailability.builder()
                .providerId(request.getProviderId())
                .startTime(utcStartTime)
                .endTime(utcEndTime)
                .timezone(request.getTimezone())
                .recurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : ProviderAvailability.RecurrenceType.NONE)
                .recurrenceDays(request.getRecurrenceDays())
                .recurrenceEndDate(request.getRecurrenceEndDate() != null ? 
                    request.getRecurrenceEndDate().atZone(providerZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null)
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .location(request.getLocation())
                .appointmentType(request.getAppointmentType())
                .specialRequirements(request.getSpecialRequirements())
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();

        availability = availabilityRepository.save(availability);

        // Generate appointment slots
        generateAppointmentSlots(availability);

        return buildAvailabilityResponse(availability);
    }

    public ProviderAvailabilityResponse getProviderAvailability(Long providerId) {
        log.info("Fetching availability for provider: {}", providerId);

        List<ProviderAvailability> availabilities = availabilityRepository.findByProviderIdAndStatus(
                providerId, ProviderAvailability.AvailabilityStatus.ACTIVE);

        if (availabilities.isEmpty()) {
            throw new RuntimeException("No availability found for provider: " + providerId);
        }

        // For now, return the first availability. In a real system, you might want to aggregate multiple availabilities
        ProviderAvailability availability = availabilities.get(0);
        return buildAvailabilityResponse(availability);
    }

    @Transactional
    public AppointmentSlotResponse updateSlot(Long slotId, AppointmentSlotResponse updateRequest) {
        log.info("Updating slot: {}", slotId);

        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));

        // Check if slot is already booked
        if (slot.getStatus() == AppointmentSlot.SlotStatus.BOOKED) {
            throw new IllegalArgumentException("Cannot update a booked slot");
        }

        // Update fields
        if (updateRequest.getStartTime() != null) {
            slot.setStartTime(updateRequest.getStartTime());
        }
        if (updateRequest.getEndTime() != null) {
            slot.setEndTime(updateRequest.getEndTime());
        }
        if (updateRequest.getPrice() != null) {
            slot.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getCurrency() != null) {
            slot.setCurrency(updateRequest.getCurrency());
        }
        if (updateRequest.getLocation() != null) {
            slot.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getAppointmentType() != null) {
            slot.setAppointmentType(updateRequest.getAppointmentType());
        }
        if (updateRequest.getSpecialRequirements() != null) {
            slot.setSpecialRequirements(updateRequest.getSpecialRequirements());
        }
        if (updateRequest.getStatus() != null) {
            slot.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getBookingNotes() != null) {
            slot.setBookingNotes(updateRequest.getBookingNotes());
        }

        slot = slotRepository.save(slot);
        return buildSlotResponse(slot);
    }

    @Transactional
    public void deleteAvailability(Long availabilityId, boolean deleteRecurring) {
        log.info("Deleting availability: {} with recurring: {}", availabilityId, deleteRecurring);

        ProviderAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found: " + availabilityId));

        // Check for booked appointments
        long bookedSlots = slotRepository.countBookedSlotsByAvailabilityId(availabilityId);
        if (bookedSlots > 0) {
            throw new IllegalArgumentException("Cannot delete availability with booked appointments");
        }

        if (deleteRecurring && availability.getRecurrenceType() != ProviderAvailability.RecurrenceType.NONE) {
            // Delete all related slots
            List<AppointmentSlot> slots = slotRepository.findByProviderAvailabilityId(availabilityId);
            slotRepository.deleteAll(slots);
            availabilityRepository.delete(availability);
        } else {
            // Mark as deleted instead of physical deletion
            availability.setStatus(ProviderAvailability.AvailabilityStatus.DELETED);
            availabilityRepository.save(availability);
        }
    }

    public List<AppointmentSlotResponse> searchAvailability(AvailabilitySearchRequest request) {
        log.info("Searching availability with filters: {}", request);

        // Convert dates to UTC
        ZoneId searchZone = request.getTimezone() != null ? ZoneId.of(request.getTimezone()) : ZoneId.systemDefault();
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay().atZone(searchZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endDateTime = request.getEndDate() != null ? 
            request.getEndDate().atTime(23, 59, 59).atZone(searchZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() :
            startDateTime.plusDays(30); // Default to 30 days if no end date

        List<AppointmentSlot> availableSlots = slotRepository.findAvailableSlotsByFilters(
                startDateTime, endDateTime, request.getLocation(), request.getAppointmentType(),
                request.getProviderId(), request.getMaxPrice(), request.getSlotDurationMinutes());

        return availableSlots.stream()
                .map(this::buildSlotResponse)
                .collect(Collectors.toList());
    }

    private void generateAppointmentSlots(ProviderAvailability availability) {
        log.info("Generating appointment slots for availability: {}", availability.getId());

        List<AppointmentSlot> slots = new ArrayList<>();
        ZoneId providerZone = ZoneId.of(availability.getTimezone());

        ProviderAvailability.RecurrenceType recurrenceType = availability.getRecurrenceType();
        if (recurrenceType == null || recurrenceType == ProviderAvailability.RecurrenceType.NONE) {
            // Generate slots for single availability
            slots.addAll(generateSlotsForTimeRange(availability, availability.getStartTime(), availability.getEndTime()));
        } else {
            // Generate recurring slots
            LocalDateTime currentStart = availability.getStartTime();
            LocalDateTime currentEnd = availability.getEndTime();
            LocalDateTime endDate = availability.getRecurrenceEndDate() != null ? 
                availability.getRecurrenceEndDate() : currentStart.plusMonths(6); // Default to 6 months

            while (currentStart.isBefore(endDate)) {
                slots.addAll(generateSlotsForTimeRange(availability, currentStart, currentEnd));

                // Calculate next occurrence
                switch (recurrenceType) {
                    case DAILY:
                        currentStart = currentStart.plusDays(1);
                        currentEnd = currentEnd.plusDays(1);
                        break;
                    case WEEKLY:
                        if (availability.getRecurrenceDays() != null && !availability.getRecurrenceDays().isEmpty()) {
                            // Find next occurrence based on recurrence days
                            currentStart = findNextOccurrence(currentStart, availability.getRecurrenceDays());
                            currentEnd = findNextOccurrence(currentEnd, availability.getRecurrenceDays());
                        } else {
                            currentStart = currentStart.plusWeeks(1);
                            currentEnd = currentEnd.plusWeeks(1);
                        }
                        break;
                    case MONTHLY:
                        currentStart = currentStart.plusMonths(1);
                        currentEnd = currentEnd.plusMonths(1);
                        break;
                }
            }
        }

        slotRepository.saveAll(slots);
        log.info("Generated {} appointment slots", slots.size());
    }

    private List<AppointmentSlot> generateSlotsForTimeRange(ProviderAvailability availability, 
                                                           LocalDateTime startTime, LocalDateTime endTime) {
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalDateTime currentSlotStart = startTime;
        
        while (currentSlotStart.plusMinutes(availability.getSlotDurationMinutes()).isBefore(endTime) ||
               currentSlotStart.plusMinutes(availability.getSlotDurationMinutes()).isEqual(endTime)) {
            
            LocalDateTime slotEnd = currentSlotStart.plusMinutes(availability.getSlotDurationMinutes());
            
            AppointmentSlot slot = AppointmentSlot.builder()
                    .providerAvailability(availability)
                    .providerId(availability.getProviderId())
                    .startTime(currentSlotStart)
                    .endTime(slotEnd)
                    .timezone(availability.getTimezone())
                    .status(AppointmentSlot.SlotStatus.AVAILABLE)
                    .price(availability.getPrice())
                    .currency(availability.getCurrency())
                    .location(availability.getLocation())
                    .appointmentType(availability.getAppointmentType())
                    .specialRequirements(availability.getSpecialRequirements())
                    .build();

            slots.add(slot);
            currentSlotStart = slotEnd;
        }

        return slots;
    }

    private LocalDateTime findNextOccurrence(LocalDateTime current, Set<DayOfWeek> recurrenceDays) {
        LocalDateTime next = current.plusDays(1);
        while (!recurrenceDays.contains(next.getDayOfWeek())) {
            next = next.plusDays(1);
        }
        return next;
    }

    private ProviderAvailabilityResponse buildAvailabilityResponse(ProviderAvailability availability) {
        List<AppointmentSlot> slots = slotRepository.findByProviderAvailabilityId(availability.getId());
        
        long totalSlots = slots.size();
        long availableSlots = slots.stream().filter(s -> s.getStatus() == AppointmentSlot.SlotStatus.AVAILABLE).count();
        long bookedSlots = slots.stream().filter(s -> s.getStatus() == AppointmentSlot.SlotStatus.BOOKED).count();
        long cancelledSlots = slots.stream().filter(s -> s.getStatus() == AppointmentSlot.SlotStatus.CANCELLED).count();

        List<AppointmentSlotResponse> slotResponses = slots.stream()
                .map(this::buildSlotResponse)
                .collect(Collectors.toList());

        return ProviderAvailabilityResponse.builder()
                .id(availability.getId())
                .providerId(availability.getProviderId())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .timezone(availability.getTimezone())
                .recurrenceType(availability.getRecurrenceType())
                .recurrenceDays(availability.getRecurrenceDays())
                .recurrenceEndDate(availability.getRecurrenceEndDate())
                .slotDurationMinutes(availability.getSlotDurationMinutes())
                .price(availability.getPrice())
                .currency(availability.getCurrency())
                .location(availability.getLocation())
                .appointmentType(availability.getAppointmentType())
                .specialRequirements(availability.getSpecialRequirements())
                .status(availability.getStatus())
                .notes(availability.getNotes())
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .cancelledSlots(cancelledSlots)
                .appointmentSlots(slotResponses)
                .build();
    }

    private AppointmentSlotResponse buildSlotResponse(AppointmentSlot slot) {
        return AppointmentSlotResponse.builder()
                .id(slot.getId())
                .providerId(slot.getProviderId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .timezone(slot.getTimezone())
                .status(slot.getStatus())
                .price(slot.getPrice())
                .currency(slot.getCurrency())
                .location(slot.getLocation())
                .appointmentType(slot.getAppointmentType())
                .specialRequirements(slot.getSpecialRequirements())
                .patientId(slot.getPatientId())
                .bookingNotes(slot.getBookingNotes())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
} 