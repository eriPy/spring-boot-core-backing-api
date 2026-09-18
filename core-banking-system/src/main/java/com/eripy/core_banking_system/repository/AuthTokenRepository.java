package com.eripy.core_banking_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.AuthToken;
import com.eripy.core_banking_system.model.enums.TokenStatus;
import com.eripy.core_banking_system.model.enums.TokenType;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    List<AuthToken> findTop5ByAccountOrderByGeneratedTimeDesc(
        Account account
    );
    Optional<AuthToken> findByAccountAndTokenContentAndStatusAndTokenType(
        Account account,
        String tokenContent,
        TokenStatus status,
        TokenType tokenType
    );
}