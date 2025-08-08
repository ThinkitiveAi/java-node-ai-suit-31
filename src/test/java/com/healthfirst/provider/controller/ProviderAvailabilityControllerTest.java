package com.healthfirst.provider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthfirst.provider.config.TestSecurityConfig;
import com.healthfirst.provider.dto.AppointmentSlotResponse;
import com.healthfirst.provider.dto.AvailabilitySearchRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityRequest;
import com.healthfirst.provider.dto.ProviderAvailabilityResponse;
import com.healthfirst.provider.entity.AppointmentSlot;
import com.healthfirst.provider.entity.ProviderAvailability;
import com.healthfirst.provider.service.ProviderAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderAvailabilityController.class)
@Import(TestSecurityConfig.class)
class ProviderAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderAvailabilityService availabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProviderAvailabilityRequest validRequest;
    private ProviderAvailabilityResponse mockResponse;
    private AppointmentSlotResponse mockSlotResponse;

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

        mockResponse = ProviderAvailabilityResponse.builder()
                .id(1L)
                .providerId(1L)
                .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 22, 0))
                .timezone("America/New_York")
                .slotDurationMinutes(30)
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .location("New York Medical Center")
                .appointmentType("CONSULTATION")
                .status(ProviderAvailability.AvailabilityStatus.ACTIVE)
                .totalSlots(16L)
                .availableSlots(14L)
                .bookedSlots(2L)
                .cancelledSlots(0L)
                .build();

        mockSlotResponse = AppointmentSlotResponse.builder()
                .id(1L)
                .providerId(1L)
                .startTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 14, 30))
                .status(AppointmentSlot.SlotStatus.AVAILABLE)
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .location("New York Medical Center")
                .appointmentType("CONSULTATION")
                .build();
    }

    @Test
    void createAvailability_Success() throws Exception {
        // Arrange
        when(availabilityService.createAvailability(any(ProviderAvailabilityRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/provider/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.providerId").value(1))
                .andExpect(jsonPath("$.totalSlots").value(16))
                .andExpect(jsonPath("$.availableSlots").value(14))
                .andExpect(jsonPath("$.bookedSlots").value(2));

        verify(availabilityService).createAvailability(any(ProviderAvailabilityRequest.class));
    }

    @Test
    void createAvailability_InvalidRequest_BadRequest() throws Exception {
        // Arrange
        validRequest.setStartTime(LocalDateTime.of(2024, 1, 15, 17, 0));
        validRequest.setEndTime(LocalDateTime.of(2024, 1, 15, 9, 0));

        when(availabilityService.createAvailability(any(ProviderAvailabilityRequest.class)))
                .thenThrow(new IllegalArgumentException("Start time must be before end time"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/provider/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService).createAvailability(any(ProviderAvailabilityRequest.class));
    }

    @Test
    void getProviderAvailability_Success() throws Exception {
        // Arrange
        when(availabilityService.getProviderAvailability(1L))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/provider/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.providerId").value(1))
                .andExpect(jsonPath("$.totalSlots").value(16));

        verify(availabilityService).getProviderAvailability(1L);
    }

    @Test
    void getProviderAvailability_NotFound() throws Exception {
        // Arrange
        when(availabilityService.getProviderAvailability(1L))
                .thenThrow(new RuntimeException("No availability found for provider: 1"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/provider/1/availability"))
                .andExpect(status().isNotFound());

        verify(availabilityService).getProviderAvailability(1L);
    }

    @Test
    void updateSlot_Success() throws Exception {
        // Arrange
        AppointmentSlotResponse updateRequest = AppointmentSlotResponse.builder()
                .price(new BigDecimal("150.00"))
                .status(AppointmentSlot.SlotStatus.CANCELLED)
                .build();

        when(availabilityService.updateSlot(1L, updateRequest))
                .thenReturn(mockSlotResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/provider/availability/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(100.00));

        verify(availabilityService).updateSlot(1L, updateRequest);
    }

    @Test
    void updateSlot_BookedSlot_BadRequest() throws Exception {
        // Arrange
        AppointmentSlotResponse updateRequest = AppointmentSlotResponse.builder()
                .price(new BigDecimal("150.00"))
                .build();

        when(availabilityService.updateSlot(1L, updateRequest))
                .thenThrow(new IllegalArgumentException("Cannot update a booked slot"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/provider/availability/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService).updateSlot(1L, updateRequest);
    }

    @Test
    void updateSlot_NotFound() throws Exception {
        // Arrange
        AppointmentSlotResponse updateRequest = AppointmentSlotResponse.builder()
                .price(new BigDecimal("150.00"))
                .build();

        when(availabilityService.updateSlot(1L, updateRequest))
                .thenThrow(new RuntimeException("Slot not found: 1"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/provider/availability/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(availabilityService).updateSlot(1L, updateRequest);
    }

    @Test
    void deleteAvailability_Success() throws Exception {
        // Arrange
        doNothing().when(availabilityService).deleteAvailability(1L, false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/provider/availability/1")
                        .param("deleteRecurring", "false"))
                .andExpect(status().isNoContent());

        verify(availabilityService).deleteAvailability(1L, false);
    }

    @Test
    void deleteAvailability_WithBookedSlots_BadRequest() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Cannot delete availability with booked appointments"))
                .when(availabilityService).deleteAvailability(1L, false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/provider/availability/1")
                        .param("deleteRecurring", "false"))
                .andExpect(status().isBadRequest());

        verify(availabilityService).deleteAvailability(1L, false);
    }

    @Test
    void deleteAvailability_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Availability not found: 1"))
                .when(availabilityService).deleteAvailability(1L, false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/provider/availability/1")
                        .param("deleteRecurring", "false"))
                .andExpect(status().isNotFound());

        verify(availabilityService).deleteAvailability(1L, false);
    }

    @Test
    void searchAvailability_Success() throws Exception {
        // Arrange
        AvailabilitySearchRequest searchRequest = AvailabilitySearchRequest.builder()
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 1, 20))
                .location("New York")
                .appointmentType("CONSULTATION")
                .timezone("America/New_York")
                .build();

        List<AppointmentSlotResponse> mockSlots = Arrays.asList(mockSlotResponse);
        when(availabilityService.searchAvailability(any(AvailabilitySearchRequest.class)))
                .thenReturn(mockSlots);

        // Act & Assert
        mockMvc.perform(get("/api/v1/availability/search")
                        .param("startDate", "2024-01-15")
                        .param("endDate", "2024-01-20")
                        .param("location", "New York")
                        .param("appointmentType", "CONSULTATION")
                        .param("timezone", "America/New_York"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].providerId").value(1));

        verify(availabilityService).searchAvailability(any(AvailabilitySearchRequest.class));
    }

    @Test
    void searchAvailability_WithFilters_Success() throws Exception {
        // Arrange
        List<AppointmentSlotResponse> mockSlots = Arrays.asList(mockSlotResponse);
        when(availabilityService.searchAvailability(any(AvailabilitySearchRequest.class)))
                .thenReturn(mockSlots);

        // Act & Assert
        mockMvc.perform(get("/api/v1/availability/search")
                        .param("startDate", "2024-01-15")
                        .param("providerId", "1")
                        .param("maxPrice", "200")
                        .param("slotDurationMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(availabilityService).searchAvailability(any(AvailabilitySearchRequest.class));
    }

    @Test
    void getProviderSlots_Success() throws Exception {
        // Arrange
        mockResponse.setAppointmentSlots(Arrays.asList(mockSlotResponse));
        when(availabilityService.getProviderAvailability(1L))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/provider/1/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].providerId").value(1));

        verify(availabilityService).getProviderAvailability(1L);
    }

    @Test
    void getProviderSlots_NotFound() throws Exception {
        // Arrange
        when(availabilityService.getProviderAvailability(1L))
                .thenThrow(new RuntimeException("No availability found for provider: 1"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/provider/1/slots"))
                .andExpect(status().isNotFound());

        verify(availabilityService).getProviderAvailability(1L);
    }

    @Test
    void createAvailability_ValidationError() throws Exception {
        // Arrange - Missing required fields
        ProviderAvailabilityRequest invalidRequest = new ProviderAvailabilityRequest();

        // Act & Assert
        mockMvc.perform(post("/api/v1/provider/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).createAvailability(any());
    }

    @Test
    void searchAvailability_ValidationError() throws Exception {
        // Arrange - Missing required startDate
        AvailabilitySearchRequest invalidRequest = new AvailabilitySearchRequest();

        // Act & Assert
        mockMvc.perform(get("/api/v1/availability/search")
                        .param("location", "New York"))
                .andExpect(status().isBadRequest());

        verify(availabilityService, never()).searchAvailability(any());
    }
} 