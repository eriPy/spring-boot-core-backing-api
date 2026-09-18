package com.eripy.core_banking_system.model;

import java.math.BigDecimal;
import com.eripy.core_banking_system.model.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "accounts")
@Setter 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Account {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance", nullable = false)
    @NotNull(message = "The balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "The price must be greater than or equals to 0")
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, insertable = false)
    @NotNull(message = "The status is required")
    private AccountStatus status;
}