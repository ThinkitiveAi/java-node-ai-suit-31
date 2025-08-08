package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityResponse;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Availability Management", description = "APIs for managing provider availability and appointment slots")
public class ProviderAvailabilityController {

    private final ProviderAvailabilityService availabilityService;

    @PostMapping("/provider/availability")
    @Operation(summary = "Create provider availability", 
               description = "Create availability and auto-generate appointment slots with support for recurrence and timezone handling")
    public ResponseEntity<ProviderAvailabilityResponse> createAvailability(
            @Valid @RequestBody ProviderAvailabilityRequest request) {
        log.info("Creating availability for provider: {}", request.getProviderId());
        
        try {
            ProviderAvailabilityResponse response = availabilityService.createAvailability(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for creating availability: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/provider/{providerId}/availability")
    @Operation(summary = "Get provider availability", 
               description = "Return availability with slot counts by status and detailed slot data")
    public ResponseEntity<ProviderAvailabilityResponse> getProviderAvailability(
            @Parameter(description = "Provider ID") @PathVariable Long providerId) {
        log.info("Fetching availability for provider: {}", providerId);
        
        try {
            ProviderAvailabilityResponse response = availabilityService.getProviderAvailability(providerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching availability for provider {}: {}", providerId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/provider/availability/{slotId}")
    @Operation(summary = "Update appointment slot", 
               description = "Update slot timing, status, pricing, or notes")
    public ResponseEntity<AppointmentSlotResponse> updateSlot(
            @Parameter(description = "Slot ID") @PathVariable Long slotId,
            @Valid @RequestBody AppointmentSlotResponse updateRequest) {
        log.info("Updating slot: {}", slotId);
        
        try {
            AppointmentSlotResponse response = availabilityService.updateSlot(slotId, updateRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for updating slot {}: {}", slotId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Slot not found: {}", slotId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating slot: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/provider/availability/{availabilityId}")
    @Operation(summary = "Delete provider availability", 
               description = "Optionally delete all recurring slots; must check for booked appointments")
    public ResponseEntity<Void> deleteAvailability(
            @Parameter(description = "Availability ID") @PathVariable Long availabilityId,
            @Parameter(description = "Delete all recurring slots") @RequestParam(defaultValue = "false") boolean deleteRecurring) {
        log.info("Deleting availability: {} with recurring: {}", availabilityId, deleteRecurring);
        
        try {
            availabilityService.deleteAvailability(availabilityId, deleteRecurring);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Cannot delete availability {}: {}", availabilityId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Availability not found: {}", availabilityId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/availability/search")
    @Operation(summary = "Search available slots", 
               description = "Patients can search for available slots by location, date, appointment type, etc.")
    public ResponseEntity<List<AppointmentSlotResponse>> searchAvailability(
            @Valid @ModelAttribute AvailabilitySearchRequest request) {
        log.info("Searching availability with filters: {}", request);
        
        try {
            List<AppointmentSlotResponse> response = availabilityService.searchAvailability(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/provider/{providerId}/slots")
    @Operation(summary = "Get provider slots", 
               description = "Get all appointment slots for a specific provider")
    public ResponseEntity<List<AppointmentSlotResponse>> getProviderSlots(
            @Parameter(description = "Provider ID") @PathVariable Long providerId,
            @Parameter(description = "Slot status filter") @RequestParam(required = false) String status) {
        log.info("Fetching slots for provider: {} with status: {}", providerId, status);
        
        try {
            // This would need to be implemented in the service
            // For now, we'll return the slots from the availability response
            ProviderAvailabilityResponse availability = availabilityService.getProviderAvailability(providerId);
            return ResponseEntity.ok(availability.getAppointmentSlots());
        } catch (RuntimeException e) {
            log.error("Error fetching slots for provider {}: {}", providerId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 