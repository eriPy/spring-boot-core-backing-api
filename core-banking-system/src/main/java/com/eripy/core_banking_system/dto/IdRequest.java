package com.eripy.core_banking_system.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IdRequest(
    @NotNull(message = "The id is required")
    @Positive(message = "The id must be positive")
    Long id
) {}