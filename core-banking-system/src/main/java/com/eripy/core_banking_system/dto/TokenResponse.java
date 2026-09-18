package com.eripy.core_banking_system.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eripy.core_banking_system.exception.InvalidOperation;

public record TokenResponse(String token) {
    private static final Logger logger = LoggerFactory.getLogger(TokenResponse.class);
    public TokenResponse {
        if (token == null || token.isBlank()) {
            logger.error("Something went wrong with the token creation");
            throw new InvalidOperation("Failed to use the token");
        }
    }
}