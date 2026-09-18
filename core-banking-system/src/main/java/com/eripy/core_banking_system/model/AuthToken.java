package com.eripy.core_banking_system.model;

import java.time.LocalDateTime;
import com.eripy.core_banking_system.model.enums.TokenStatus;
import com.eripy.core_banking_system.model.enums.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity 
@Table(name = "auth_tokens")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "token_content", nullable = false)
    @NotBlank(message = "Token is required")
    @Size(min = 6, max = 6, message = "Token must be 6 length")
    private String tokenContent;

    @Column(name = "generated_time", insertable = false)
    private LocalDateTime generatedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", insertable = false)
    private TokenStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    @NotNull(message = "The token type is required")
    private TokenType tokenType;

    public AuthToken(
        Account account,
        String tokenContent,
        TokenType tokenType
    ) {
        this.account = account;
        this.tokenContent = tokenContent;
        this.tokenType = tokenType;
    }
}
