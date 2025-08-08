package com.healthfirst.provider.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderRegistrationRequest {
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

    @Size(min = 3, max = 100)
    @NotBlank
    private String specialization;

    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @NotBlank
    private String licenseNumber;

    @Min(0)
    @Max(50)
    private int yearsOfExperience;

    @NotNull
    private ClinicAddressDto clinicAddress;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClinicAddressDto {
        @NotBlank
        private String street;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        private String zip;
    }
} 