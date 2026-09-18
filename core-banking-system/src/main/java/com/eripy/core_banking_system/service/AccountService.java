package com.eripy.core_banking_system.service;

import java.math.BigDecimal;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.eripy.core_banking_system.dto.IdRequest;
import com.eripy.core_banking_system.dto.TokenResponse;
import com.eripy.core_banking_system.dto.TokenToDisableRequest;
import com.eripy.core_banking_system.exception.AccountException;
import com.eripy.core_banking_system.exception.InvalidOperation;
import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.Operation;
import com.eripy.core_banking_system.model.enums.AccountStatus;
import com.eripy.core_banking_system.repository.AccountRepository;
import com.eripy.core_banking_system.repository.OperationRepository;

@Service 
public class AccountService {
    private final SecurityService securityService;
    private final OperationRepository operationRepository;
    private final TokenService tokenService;
    private final SecretKey secretKey;
    private final AccountRepository accountRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AccountService(
        SecurityService securityService, 
        TokenService tokenService, 
        SecretKey secretKey,
        OperationRepository operationRepository,
        AccountRepository accountRepository
    ) {
        this.securityService = securityService; 
        this.tokenService = tokenService; 
        this.secretKey = secretKey;
        this.operationRepository = operationRepository;
        this.accountRepository = accountRepository;   
    }

    public TokenResponse verifyAccount(IdRequest idRequest) {
        securityService.accountIsAvailable(idRequest.id());
        logger.info("All data is valid");
        return new TokenResponse(
            tokenService.createJwtToken(
                idRequest, 
                jwtExpiration, 
                secretKey
            )
        );
    }
    
    public List<Operation> transactionsFind(IdRequest idRequest) {
        logger.info("Search account transaction");
        return operationRepository.findByPayer(idRequest.id());
    }

    public Operation findTransaction(IdRequest idRequest) {
        logger.info("Search Transaction");
        Operation operation = operationRepository.findById(idRequest.id())
            .orElseThrow(() -> {
                logger.info("The operation not find on database");
                throw new AccountException("Operation not find");
            });
        logger.info("Operation was find");
        return operation;
    }

    public void disableAccount(Long id, TokenToDisableRequest token) {
        securityService.validJwtInRedis(id, "account", token.jwtToken());
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountException("Account is invalidate"));
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn("This account cannot be close because has cash");
            throw new InvalidOperation("Invalid operation");
        }
        account.setStatus(AccountStatus.close);
        accountRepository.save(account);
    }
}
