package com.healthfirst.provider.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRegistrationRequest {
    @Size(min = 2, max = 50)
    @NotBlank
    private String firstName;

    @Size(min = 2, max = 50)
    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotNull
    private Gender gender;

    @NotNull
    private AddressDto address;

    private EmergencyContactDto emergencyContact;

    private List<String> medicalHistory;

    private InsuranceInfoDto insuranceInfo;

    public enum Gender {
        male, female, other, prefer_not_to_say
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDto {
        @NotBlank
        private String street;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        private String zip;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmergencyContactDto {
        private String name;
        private String phone;
        private String relationship;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InsuranceInfoDto {
        private String provider;
        private String policyNumber;
    }
} 