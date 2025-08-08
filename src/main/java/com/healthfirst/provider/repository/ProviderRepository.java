package com.healthfirst.provider.repository;

import com.healthfirst.provider.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    Optional<Provider> findByEmail(String email);
    Optional<Provider> findByPhoneNumber(String phoneNumber);
    Optional<Provider> findByLicenseNumber(String licenseNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByLicenseNumber(String licenseNumber);
} 