package com.healthfirst.provider.service;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;
import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.repository.PatientRepository;
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
public class PatientService {
    private final PatientRepository patientRepository;
    private final Validator validator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        validateRequest(request);
        checkUniqueFields(request);
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName().trim());
        patient.setLastName(request.getLastName().trim());
        patient.setEmail(request.getEmail().trim().toLowerCase());
        patient.setPhoneNumber(request.getPhoneNumber().trim());
        patient.setPasswordHash(hashedPassword);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(Patient.Gender.valueOf(request.getGender().name()));
        
        // Set address
        Patient.Address address = new Patient.Address();
        address.setStreet(request.getAddress().getStreet().trim());
        address.setCity(request.getAddress().getCity().trim());
        address.setState(request.getAddress().getState().trim());
        address.setZip(request.getAddress().getZip().trim());
        patient.setAddress(address);
        
        // Set emergency contact if provided
        if (request.getEmergencyContact() != null) {
            Patient.EmergencyContact emergencyContact = new Patient.EmergencyContact();
            emergencyContact.setName(request.getEmergencyContact().getName() != null ? 
                request.getEmergencyContact().getName().trim() : null);
            emergencyContact.setPhone(request.getEmergencyContact().getPhone() != null ? 
                request.getEmergencyContact().getPhone().trim() : null);
            emergencyContact.setRelationship(request.getEmergencyContact().getRelationship() != null ? 
                request.getEmergencyContact().getRelationship().trim() : null);
            patient.setEmergencyContact(emergencyContact);
        }
        
        // Set medical history if provided
        if (request.getMedicalHistory() != null) {
            patient.setMedicalHistory(request.getMedicalHistory());
        }
        
        // Set insurance info if provided
        if (request.getInsuranceInfo() != null) {
            Patient.InsuranceInfo insuranceInfo = new Patient.InsuranceInfo();
            insuranceInfo.setProvider(request.getInsuranceInfo().getProvider() != null ? 
                request.getInsuranceInfo().getProvider().trim() : null);
            insuranceInfo.setPolicyNumber(request.getInsuranceInfo().getPolicyNumber() != null ? 
                request.getInsuranceInfo().getPolicyNumber().trim() : null);
            patient.setInsuranceInfo(insuranceInfo);
        }
        
        patient.setEmailVerified(false);
        patient.setPhoneVerified(false);
        patient.setActive(true);
        
        try {
            patient = patientRepository.save(patient);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Duplicate entry detected.");
        }
        return mapToResponse(patient);
    }

    public Patient loginPatient(PatientLoginRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        Patient patient = patientRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!patient.isActive()) {
            throw new IllegalStateException("Patient account is not active");
        }
        if (!passwordEncoder.matches(request.getPassword(), patient.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return patient;
    }

    private void validateRequest(PatientRegistrationRequest request) {
        Set<ConstraintViolation<PatientRegistrationRequest>> violations = validator.validate(request);
        if (!isStrongPassword(request.getPassword())) {
            throw new ConstraintViolationException("Password does not meet strength requirements", new HashSet<>());
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void checkUniqueFields(PatientRegistrationRequest request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (patientRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[^a-zA-Z0-9].*");
    }

    private PatientRegistrationResponse mapToResponse(Patient patient) {
        return PatientRegistrationResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .phoneNumber(patient.getPhoneNumber())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(PatientRegistrationRequest.Gender.valueOf(patient.getGender().name()))
                .address(PatientRegistrationResponse.AddressDto.builder()
                        .street(patient.getAddress().getStreet())
                        .city(patient.getAddress().getCity())
                        .state(patient.getAddress().getState())
                        .zip(patient.getAddress().getZip())
                        .build())
                .emergencyContact(patient.getEmergencyContact() != null ? 
                    PatientRegistrationResponse.EmergencyContactDto.builder()
                        .name(patient.getEmergencyContact().getName())
                        .phone(patient.getEmergencyContact().getPhone())
                        .relationship(patient.getEmergencyContact().getRelationship())
                        .build() : null)
                .medicalHistory(patient.getMedicalHistory())
                .insuranceInfo(patient.getInsuranceInfo() != null ? 
                    PatientRegistrationResponse.InsuranceInfoDto.builder()
                        .provider(patient.getInsuranceInfo().getProvider())
                        .policyNumber(patient.getInsuranceInfo().getPolicyNumber())
                        .build() : null)
                .emailVerified(patient.isEmailVerified())
                .phoneVerified(patient.isPhoneVerified())
                .isActive(patient.isActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
} 