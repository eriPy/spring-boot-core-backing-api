package com.eripy.core_banking_system.dto;

import java.math.BigDecimal;
import com.eripy.core_banking_system.model.enums.OperationType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OperationRequest(
    @Enumerated(EnumType.STRING)
    @NotNull (message = "The status is required")
    OperationType operation,
    @DecimalMin(value = "0.0", inclusive = false, message = "The amount must be greater than or equals to 0")
    BigDecimal amount,
    @NotBlank(message = "The token is required")
    String token,
    Long payee_id
) {}