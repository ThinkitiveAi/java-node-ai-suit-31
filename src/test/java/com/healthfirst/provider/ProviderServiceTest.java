package com.healthfirst.provider;

import com.healthfirst.provider.dto.ProviderRegistrationRequest;
import com.healthfirst.provider.dto.ProviderLoginRequest;
import com.healthfirst.provider.entity.ClinicAddress;
import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.repository.ProviderRepository;
import com.healthfirst.provider.service.ProviderService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProviderServiceTest {
    private ProviderRepository providerRepository;
    private Validator validator;
    private BCryptPasswordEncoder passwordEncoder;
    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        providerRepository = Mockito.mock(ProviderRepository.class);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        passwordEncoder = new BCryptPasswordEncoder(12);
        providerService = new ProviderService(providerRepository, validator, passwordEncoder);
    }

    @Test
    void testStrongPasswordValidation() {
        ProviderRegistrationRequest req = getValidRequest();
        req.setPassword("weak");
        Exception ex = assertThrows(Exception.class, () -> providerService.registerProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void testInputSanitization() {
        ProviderRegistrationRequest req = getValidRequest();
        req.setFirstName(" John ");
        req.setLastName(" Doe ");
        when(providerRepository.existsByEmail(anyString())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(anyString())).thenReturn(false);
        when(providerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        var resp = providerService.registerProvider(req);
        assertEquals("John", resp.getFirstName());
        assertEquals("Doe", resp.getLastName());
    }

    @Test
    void testDuplicateEmail() {
        ProviderRegistrationRequest req = getValidRequest();
        when(providerRepository.existsByEmail(anyString())).thenReturn(true);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> providerService.registerProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void testPasswordHashing() {
        ProviderRegistrationRequest req = getValidRequest();
        when(providerRepository.existsByEmail(anyString())).thenReturn(false);
        when(providerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(providerRepository.existsByLicenseNumber(anyString())).thenReturn(false);
        when(providerRepository.save(any())).thenAnswer(i -> {
            Provider p = (Provider) i.getArgument(0);
            assertNotEquals("StrongP@ssw0rd", p.getPasswordHash());
            assertTrue(passwordEncoder.matches("StrongP@ssw0rd", p.getPasswordHash()));
            return p;
        });
        providerService.registerProvider(req);
    }

    @Test
    void testValidLogin() {
        Provider provider = Provider.builder()
                .id(java.util.UUID.randomUUID())
                .email("john.doe@example.com")
                .passwordHash(passwordEncoder.encode("StrongP@ssw0rd"))
                .isActive(true)
                .verificationStatus(Provider.VerificationStatus.VERIFIED)
                .role(Provider.Role.DOCTOR)
                .specialization("Cardiology")
                .build();
        when(providerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(provider));
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("StrongP@ssw0rd");
        Provider result = providerService.loginProvider(req);
        assertEquals(provider.getEmail(), result.getEmail());
    }

    @Test
    void testInvalidCredentials() {
        when(providerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("wrong");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> providerService.loginProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid credentials"));
    }

    @Test
    void testInactiveProvider() {
        Provider provider = Provider.builder()
                .id(java.util.UUID.randomUUID())
                .email("john.doe@example.com")
                .passwordHash(passwordEncoder.encode("StrongP@ssw0rd"))
                .isActive(false)
                .verificationStatus(Provider.VerificationStatus.VERIFIED)
                .role(Provider.Role.DOCTOR)
                .specialization("Cardiology")
                .build();
        when(providerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(provider));
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("StrongP@ssw0rd");
        Exception ex = assertThrows(IllegalStateException.class, () -> providerService.loginProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
    }

    @Test
    void testUnverifiedProvider() {
        Provider provider = Provider.builder()
                .id(java.util.UUID.randomUUID())
                .email("john.doe@example.com")
                .passwordHash(passwordEncoder.encode("StrongP@ssw0rd"))
                .isActive(true)
                .verificationStatus(Provider.VerificationStatus.PENDING)
                .role(Provider.Role.DOCTOR)
                .specialization("Cardiology")
                .build();
        when(providerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(provider));
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("StrongP@ssw0rd");
        Exception ex = assertThrows(IllegalStateException.class, () -> providerService.loginProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
    }

    @Test
    void testEmptyPassword() {
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("john.doe@example.com");
        req.setPassword("");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> providerService.loginProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void testInvalidEmailFormat() {
        ProviderLoginRequest req = new ProviderLoginRequest();
        req.setEmail("not-an-email");
        req.setPassword("StrongP@ssw0rd");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> providerService.loginProvider(req));
        assertTrue(ex.getMessage().toLowerCase().contains("email format"));
    }

    private ProviderRegistrationRequest getValidRequest() {
        return ProviderRegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+12345678901")
                .password("StrongP@ssw0rd")
                .specialization("Cardiology")
                .licenseNumber("LIC12345")
                .yearsOfExperience(10)
                .clinicAddress(ProviderRegistrationRequest.ClinicAddressDto.builder()
                        .street("123 Main St")
                        .city("Metropolis")
                        .state("NY")
                        .zip("12345")
                        .build())
                .build();
    }
} 