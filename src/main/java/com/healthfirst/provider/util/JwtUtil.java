package com.healthfirst.provider.util;

import com.healthfirst.provider.entity.Provider;
import com.healthfirst.provider.entity.Patient;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "replace_this_with_a_very_secure_secret_key_which_is_long_enough";
    private static final long EXPIRATION_TIME = 3600_000; // 1 hour in ms
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(Provider provider) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider_id", provider.getId().toString());
        claims.put("email", provider.getEmail());
        claims.put("role", provider.getRole().name());
        claims.put("specialization", provider.getSpecialization());
        claims.put("verification_status", provider.getVerificationStatus().name());
        claims.put("user_type", "PROVIDER");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(provider.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(Patient patient) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("patient_id", patient.getId().toString());
        claims.put("email", patient.getEmail());
        claims.put("first_name", patient.getFirstName());
        claims.put("last_name", patient.getLastName());
        claims.put("user_type", "PATIENT");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(patient.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public UUID extractProviderId(String token) {
        return UUID.fromString(extractAllClaims(token).get("provider_id", String.class));
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
} 