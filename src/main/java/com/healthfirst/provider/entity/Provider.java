package com.healthfirst.provider.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "providers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "phone_number"),
    @UniqueConstraint(columnNames = "license_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50)
    @NotBlank
    private String firstName;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50)
    @NotBlank
    private String lastName;

    @Column(nullable = false, unique = true)
    @Email
    @NotBlank
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @NotBlank
    private String phoneNumber;

    @Column(nullable = false)
    @NotBlank
    private String passwordHash;

    @Column(nullable = false, length = 100)
    @Size(min = 3, max = 100)
    @NotBlank
    private String specialization;

    @Column(name = "license_number", nullable = false, unique = true)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @NotBlank
    private String licenseNumber;

    @Column(nullable = false)
    @Min(0)
    @Max(50)
    private int yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.DOCTOR;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Embedded
    private ClinicAddress clinicAddress;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }

    public enum Role {
        ADMIN, DOCTOR, NURSE
    }
} 