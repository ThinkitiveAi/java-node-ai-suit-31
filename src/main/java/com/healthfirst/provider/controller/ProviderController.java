package com.healthfirst.provider.controller;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderRegistrationResponse;
import com.healthfirst.provider.service.ProviderService;
import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.dto.ProviderLoginResponse;
import com.healthfirst.provider.entity.Provider;
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
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
@Tag(name = "Provider Management", description = "APIs for healthcare provider registration and authentication")
public class ProviderController {
    private final ProviderService providerService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new healthcare provider", description = "Creates a new provider account with validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Provider registered successfully",
            content = @Content(schema = @Schema(implementation = ProviderRegistrationResponse.class))),
        @ApiResponse(responseCode = "422", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Duplicate entry (email, phone, or license already exists)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> registerProvider(@Valid @RequestBody ProviderRegistrationRequest request) {
        try {
            ProviderRegistrationResponse response = providerService.registerProvider(request);
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
    @Operation(summary = "Provider login", description = "Authenticates a provider and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = ProviderLoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active/verified"),
        @ApiResponse(responseCode = "422", description = "Validation error"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProviderLoginResponse> loginProvider(@Valid @RequestBody ProviderLoginRequest request) {
        try {
            Provider provider = providerService.loginProvider(request);
            String token = jwtUtil.generateToken(provider);
            ProviderLoginResponse response = ProviderLoginResponse.builder()
                .success(true)
                .message("Login successful")
                .data(Map.of(
                    "access_token", token,
                    "expires_in", 3600,
                    "token_type", "Bearer",
                    "provider", Map.of(
                        "id", provider.getId(),
                        "email", provider.getEmail(),
                        "role", provider.getRole(),
                        "specialization", provider.getSpecialization(),
                        "verification_status", provider.getVerificationStatus()
                    )
                ))
                .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ProviderLoginResponse response = ProviderLoginResponse.builder()
                .success(false)
                .message("Invalid credentials")
                .error_code("INVALID_CREDENTIALS")
                .build();
            return ResponseEntity.status(401).body(response);
        } catch (IllegalStateException e) {
            ProviderLoginResponse response = ProviderLoginResponse.builder()
                .success(false)
                .message(e.getMessage())
                .error_code("PROVIDER_NOT_ACTIVE_OR_VERIFIED")
                .build();
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            ProviderLoginResponse response = ProviderLoginResponse.builder()
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