package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.PatientRegistrationRequest;
import com.healthfirst.provider.dto.PatientRegistrationResponse;
import com.healthfirst.provider.dto.PatientLoginRequest;
import com.healthfirst.provider.dto.PatientLoginResponse;
import com.healthfirst.provider.entity.Patient;
import com.healthfirst.provider.service.PatientService;
import com.healthfirst.provider.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "APIs for patient registration and authentication")
public class PatientController {
    private final PatientService patientService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new patient", description = "Creates a new patient account with validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient registered successfully",
            content = @Content(schema = @Schema(implementation = PatientRegistrationResponse.class))),
        @ApiResponse(responseCode = "422", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Duplicate entry (email or phone already exists)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {
        try {
            PatientRegistrationResponse response = patientService.registerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConstraintViolationException e) {
            Map<String, String> errors = new HashMap<>();
            e.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
            return ResponseEntity.status(422).body(errors);
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return ResponseEntity.status(409).body(errors);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Internal server error");
            return ResponseEntity.status(500).body(errors);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Patient login", description = "Authenticates a patient and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = PatientLoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active"),
        @ApiResponse(responseCode = "422", description = "Validation error"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PatientLoginResponse> loginPatient(@Valid @RequestBody PatientLoginRequest request) {
        try {
            Patient patient = patientService.loginPatient(request);
            String token = jwtUtil.generateToken(patient);
            PatientLoginResponse response = PatientLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .data(Map.of(
                    "access_token", token,
                    "expires_in", 3600,
                    "token_type", "Bearer",
                    "patient", Map.of(
                        "id", patient.getId(),
                        "email", patient.getEmail(),
                        "firstName", patient.getFirstName(),
                        "lastName", patient.getLastName(),
                        "emailVerified", patient.isEmailVerified(),
                        "phoneVerified", patient.isPhoneVerified()
                    )
                ))
                .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            PatientLoginResponse response = PatientLoginResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .error_code("INVALID_CREDENTIALS")
                .build();
            return ResponseEntity.status(401).body(response);
        } catch (IllegalStateException e) {
            PatientLoginResponse response = PatientLoginResponse.builder()
                .success(false)
                .message(e.getMessage())
                .error_code("PATIENT_NOT_ACTIVE")
                .build();
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            PatientLoginResponse response = PatientLoginResponse.builder()
                .success(false)
                .message("Internal server error")
                .error_code("INTERNAL_ERROR")
                .build();
            return ResponseEntity.status(500).body(response);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(422).body(errors);
    }
} 