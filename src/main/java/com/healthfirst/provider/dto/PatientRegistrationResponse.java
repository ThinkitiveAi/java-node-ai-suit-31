package com.healthfirst.provider.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PatientRegistrationResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private PatientRegistrationRequest.Gender gender;
    private AddressDto address;
    private EmergencyContactDto emergencyContact;
    private List<String> medicalHistory;
    private InsuranceInfoDto insuranceInfo;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AddressDto {
        private String street;
        private String city;
        private String state;
        private String zip;
    }

    @Data
    @Builder
    public static class EmergencyContactDto {
        private String name;
        private String phone;
        private String relationship;
    }

    @Data
    @Builder
    public static class InsuranceInfoDto {
        private String provider;
        private String policyNumber;
    }
} 