package com.healthfirst.provider.dto;

import com.healthfirst.provider.entity.Provider.VerificationStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderRegistrationResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private int yearsOfExperience;
    private ClinicAddressDto clinicAddress;
    private VerificationStatus verificationStatus;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClinicAddressDto {
        private String street;
        private String city;
        private String state;
        private String zip;
    }
} 