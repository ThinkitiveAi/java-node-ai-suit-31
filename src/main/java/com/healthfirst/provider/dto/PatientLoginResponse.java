package com.healthfirst.provider.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class PatientLoginResponse {
    private boolean success;
    private String message;
    private String error_code;
    private Map<String, Object> data;
} 