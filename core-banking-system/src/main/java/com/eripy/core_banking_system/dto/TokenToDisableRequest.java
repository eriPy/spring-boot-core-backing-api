package com.eripy.core_banking_system.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenToDisableRequest(
    @NotBlank(message = "The token is required")
    String jwtToken
) {}
