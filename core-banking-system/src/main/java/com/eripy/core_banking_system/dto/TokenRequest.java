package com.eripy.core_banking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRequest(
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token is Invalid")
    String token
) {}
