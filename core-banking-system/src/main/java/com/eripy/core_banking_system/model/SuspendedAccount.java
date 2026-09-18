package com.eripy.core_banking_system.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "suspended_accounts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class SuspendedAccount {
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "available_until", insertable = false)
    private LocalDateTime availableUntil;

    public SuspendedAccount(Account account, LocalDateTime availableUntil) {
        this.account = account;
        this.availableUntil = availableUntil;
    }
}
