package com.eripy.core_banking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.core_banking_system.dto.IdRequest;
import com.eripy.core_banking_system.dto.OperationRequest;
import com.eripy.core_banking_system.dto.OperationResponse;
import com.eripy.core_banking_system.dto.TokenRequest;
import com.eripy.core_banking_system.dto.TokenResponse;
import com.eripy.core_banking_system.exception.AccountException;
import com.eripy.core_banking_system.exception.InvalidOperation;
import com.eripy.core_banking_system.exception.ValidationFailedException;
import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.AuthToken;
import com.eripy.core_banking_system.model.Operation;
import com.eripy.core_banking_system.model.enums.OperationType;
import com.eripy.core_banking_system.model.enums.TokenStatus;
import com.eripy.core_banking_system.model.enums.TokenType;
import com.eripy.core_banking_system.repository.AccountRepository;
import com.eripy.core_banking_system.repository.AuthTokenRepository;
import com.eripy.core_banking_system.repository.OperationRepository;

import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;

@Service 
public class OperationService {
    private final SecurityService securityService;
    private final AuthTokenRepository authTokenRepository;
    private final AccountRepository accountRepository;
    private final TokenService tokenService;
    private final SecretKey secretKey;
    private final StringRedisTemplate redisTemplate;
    private final OperationRepository operationRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public OperationService(
        SecurityService securityService,
        AuthTokenRepository authTokenRepository,
        AccountRepository accountRepository,
        TokenService tokenService,
        SecretKey secretKey,
        StringRedisTemplate redisTemplate,
        OperationRepository operationRepository
    ) {
        this.securityService = securityService;
        this.authTokenRepository = authTokenRepository;
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.secretKey = secretKey;
        this.redisTemplate = redisTemplate;
        this.operationRepository = operationRepository;
    }

    @Value ("${jwt.expiration}")
    private long jwtExpiration;

    public String generateSixDigitToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        logger.info("Token ready");
        return sb.toString();
    }

    public TokenResponse getSixDigitToken(Long id, TokenType tokenType) {
        securityService.validAccontInRedis(id, 5);
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountException("Account is invalidate"));
        logger.info("Preparing the six digit token");
        securityService.validTokenUsage(account);
        logger.info("Account data successfully validated against all requirements");
        String sixDigitToken = generateSixDigitToken();
        AuthToken newAuthToken = new AuthToken(account, sixDigitToken, tokenType);
        authTokenRepository.save(newAuthToken);
        logger.info("The token was save success");
        return new TokenResponse(sixDigitToken);
    }

    public OperationResponse getOperaionJwt(Long id, TokenRequest tokenRequest, TokenType tokenType) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountException("Account is invalidate"));
        AuthToken authToken = authTokenRepository.findByAccountAndTokenContentAndStatusAndTokenType(
            account, tokenRequest.token(), TokenStatus.available, tokenType
        ).orElseThrow(() -> {
            logger.warn("Token not find in database");
            return new ValidationFailedException("Invalid token");
        });
        if (LocalDateTime.now().minusMinutes(5).isBefore(authToken.getGeneratedTime())) {
            logger.warn("This token was created more than 5 minutes ago");
            throw new ValidationFailedException("Invalid token");
        }
        String jwtTokenAccess = tokenService.createJwtToken(
            new IdRequest(id), 
            jwtExpiration, 
            secretKey);
        redisTemplate.opsForValue().set(
            tokenType + "_account_" + id, 
            jwtTokenAccess, 
            Duration.ofMinutes(5)
        );
        return new OperationResponse(jwtTokenAccess);
    }

    private void withdrawal(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            logger.warn("Insufficient balance");
            throw new InvalidOperation("Insufficient balance for the transaction");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);      
        operationRepository.save(new Operation(OperationType.withdrawal, account, amount));  
    }

    private  void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        operationRepository.save(new Operation(OperationType.deposit, account, amount));
    }

    private void transfer(Account account, BigDecimal amount, Long payee_id) {
        if (payee_id == null) {
            logger.warn("Payee not received");
            throw new InvalidOperation("Failed with payee");
        }
        Account payeeAccount = accountRepository.findById(payee_id)
            .orElseThrow(() -> {
                logger.warn("Payee id is missing");
                return new InvalidOperation("Invalid account data");
            });
        withdrawal(account, amount);
        payeeAccount.setBalance(payeeAccount.getBalance().add(amount));
        accountRepository.save(payeeAccount);
        operationRepository.save(new Operation(OperationType.transfer, account, amount, payeeAccount));
    }

    @Transactional 
    public void processTransaction(Long id, OperationRequest req) {
        securityService.validJwtInRedis(id, "transaction", req.token());
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new InvalidOperation("Invalid account data"));
        switch (req.operation()) {
            case withdrawal:
                withdrawal(account, req.amount());
                break;
            case deposit:
                deposit(account, req.amount());
                break;
            case transfer:
                transfer(account, req.amount(), req.payee_id());
                break;
            default:
                logger.error("Invalid operation type");
                throw new InvalidOperation("Invalid Operation");
        }
    }
}
