package com.healthfirst.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLoginResponse {
    private boolean success;
    private String message;
    private Map<String, Object> data;
    private String error_code;
} 