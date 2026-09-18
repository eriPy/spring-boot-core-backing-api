package com.eripy.core_banking_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "accounts_penalties")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AccountPenalties {
    @Id   
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Min(value = 1, message = "The minimum quantity is 1")
    @Max(value = 9, message = "The maximum quantity is 9")
    @Column(name = "scale", nullable = false)
    private int strikes;

    @Min(value = 0, message = "The minimum quantity is 0")
    @Max(value = 3, message = "The maximum quantity is 3")
    @Column(name = "bans", nullable = false)
    private int bans;

    public AccountPenalties(Account account, int strike, int ban) {
        this.account = account;
        strikes = strike;
        bans = ban;
    }
}
