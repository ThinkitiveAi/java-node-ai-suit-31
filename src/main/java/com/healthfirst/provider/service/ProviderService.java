package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderRegistrationResponse;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.entity.Provider.VerificationStatus;
import com.healthfirst.provider.entity.ClinicAddress;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.dto.ProviderLoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final Validator validator;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final Set<String> ALLOWED_SPECIALIZATIONS = Set.of(
        "Cardiology", "Dermatology", "Pediatrics", "General Medicine", "Orthopedics", "Neurology", "Psychiatry", "Oncology", "Gynecology", "Ophthalmology"
    );

    @Transactional
    public ProviderRegistrationResponse registerProvider(ProviderRegistrationRequest request) {
        validateRequest(request);
        checkUniqueFields(request);
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Provider provider = Provider.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phoneNumber(request.getPhoneNumber().trim())
                .passwordHash(hashedPassword)
                .specialization(request.getSpecialization().trim())
                .licenseNumber(request.getLicenseNumber().trim())
                .yearsOfExperience(request.getYearsOfExperience())
                .clinicAddress(ClinicAddress.builder()
                        .street(request.getClinicAddress().getStreet().trim())
                        .city(request.getClinicAddress().getCity().trim())
                        .state(request.getClinicAddress().getState().trim())
                        .zip(request.getClinicAddress().getZip().trim())
                        .build())
                .verificationStatus(VerificationStatus.VERIFIED)
                .role(Provider.Role.DOCTOR)
                .isActive(true)
                .build();
        try {
            provider = providerRepository.save(provider);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Duplicate entry detected.");
        }
        return mapToResponse(provider);
    }

    public Provider loginProvider(ProviderLoginRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        Provider provider = providerRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!provider.isActive() || provider.getVerificationStatus() != Provider.VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Provider is not active or not verified");
        }
        if (!passwordEncoder.matches(request.getPassword(), provider.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return provider;
    }

    private void validateRequest(ProviderRegistrationRequest request) {
        Set<ConstraintViolation<ProviderRegistrationRequest>> violations = validator.validate(request);
        if (!ALLOWED_SPECIALIZATIONS.contains(request.getSpecialization())) {
            Set<ConstraintViolation<ProviderRegistrationRequest>> newViolations = new HashSet<>(violations);
            throw new ConstraintViolationException("Invalid specialization", newViolations);
        }
        if (!isStrongPassword(request.getPassword())) {
            throw new ConstraintViolationException("Password does not meet strength requirements", new HashSet<>());
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void checkUniqueFields(ProviderRegistrationRequest request) {
        if (providerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (providerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (providerRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists");
        }
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[^a-zA-Z0-9].*");
    }

    private ProviderRegistrationResponse mapToResponse(Provider provider) {
        return ProviderRegistrationResponse.builder()
                .id(provider.getId())
                .firstName(provider.getFirstName())
                .lastName(provider.getLastName())
                .email(provider.getEmail())
                .phoneNumber(provider.getPhoneNumber())
                .specialization(provider.getSpecialization())
                .licenseNumber(provider.getLicenseNumber())
                .yearsOfExperience(provider.getYearsOfExperience())
                .clinicAddress(ProviderRegistrationResponse.ClinicAddressDto.builder()
                        .street(provider.getClinicAddress().getStreet())
                        .city(provider.getClinicAddress().getCity())
                        .state(provider.getClinicAddress().getState())
                        .zip(provider.getClinicAddress().getZip())
                        .build())
                .verificationStatus(provider.getVerificationStatus())
                .isActive(provider.isActive())
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .build();
    }
} 