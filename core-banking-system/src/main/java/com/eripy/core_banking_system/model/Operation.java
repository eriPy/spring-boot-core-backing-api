package com.eripy.core_banking_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.eripy.core_banking_system.model.enums.OperationType;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(
    name = "operations",
    check = {
        @CheckConstraint(
            name = "operations_payee_check",
            constraint = "operation_type <> 'transfer' OR payee_id IS NOT NULL"
        )
    }
)  
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class Operation {
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    @NotNull(message = "The operation type is required")
    private OperationType operationType;

    @ManyToOne 
    @JoinColumn(name = "payer_id", nullable = false)
    private Account payer;

    @ManyToOne 
    @JoinColumn(name = "payee_id")
    private Account payee;

    @Column(name = "amount", nullable = false)
    @NotNull(message = "The amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "The amount must be greater than or equals to 0")
    private BigDecimal amount;

    @Column(name = "operation_time", insertable = false)
    private LocalDateTime operationTime;

    public Operation(OperationType operationType, Account payer, BigDecimal amount) {
        this.operationType = operationType;
        this.payer = payer;
        this.amount = amount;
    }

    public Operation(OperationType operationType, Account payer, BigDecimal amount, Account payee) {
        this(operationType, payer, amount);
        this.payee = payee;
    }
}
