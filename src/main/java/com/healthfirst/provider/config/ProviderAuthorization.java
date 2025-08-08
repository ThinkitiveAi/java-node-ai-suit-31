package com.healthfirst.provider.config;

import io.jsonwebtoken.Claims;

public class ProviderAuthorization {
    public static boolean hasRole(Claims claims, String requiredRole) {
        String role = claims.get("role", String.class);
        return role != null && role.equals(requiredRole);
    }

    public static boolean isVerified(Claims claims) {
        String status = claims.get("verification_status", String.class);
        return "VERIFIED".equals(status);
    }
} 