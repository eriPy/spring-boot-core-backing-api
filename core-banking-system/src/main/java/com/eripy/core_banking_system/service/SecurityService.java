package com.eripy.core_banking_system.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.core_banking_system.exception.AccountException;
import com.eripy.core_banking_system.exception.RateLimitExceededException;
import com.eripy.core_banking_system.exception.ValidationFailedException;
import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.AccountPenalties;
import com.eripy.core_banking_system.model.AuthToken;
import com.eripy.core_banking_system.model.SuspendedAccount;
import com.eripy.core_banking_system.model.enums.AccountStatus;
import com.eripy.core_banking_system.repository.AccountPenaltiesRepository;
import com.eripy.core_banking_system.repository.AccountRepository;
import com.eripy.core_banking_system.repository.AuthTokenRepository;
import com.eripy.core_banking_system.repository.SuspendedAccountRepository;
import jakarta.transaction.Transactional;

@Service 
public class SecurityService {
    private final AccountRepository accountRepository;
    private final SuspendedAccountRepository suspendedAccountRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AccountPenaltiesRepository accountPenaltiesRepository;
    private final StringRedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    public SecurityService(
        AccountRepository accountRepository,
        SuspendedAccountRepository suspendedAccountRepository,
        AuthTokenRepository authTokenRepository,
        AccountPenaltiesRepository accountPenaltiesRepository,
        StringRedisTemplate redisTemplate
    ) {
        this.accountRepository = accountRepository;
        this.suspendedAccountRepository = suspendedAccountRepository;
        this.authTokenRepository = authTokenRepository;
        this.accountPenaltiesRepository = accountPenaltiesRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional 
    public Account accountIsAvailable(Long id) {
        boolean revised = false;
        logger.info(
            "Search account"
        );
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Account not find");
                return new AccountException("Invalid account data");
            });
        logger.info("Search account in suspended accounts data");
        Optional<SuspendedAccount> accountIsSuspended = suspendedAccountRepository.findById(account.getId());
        if (accountIsSuspended.isPresent()) {
            revised = true;
            SuspendedAccount isSuspended = accountIsSuspended.get();
            if (LocalDateTime.now().isBefore(isSuspended.getAvailableUntil())) {
                logger.warn("Account still suspended");
                throw new AccountException("Invalid account data");
            }
            logger.info("Account is available again");
            suspendedAccountRepository.delete(isSuspended);
            logger.info("Account was delete from suspended data");
            account.setStatus(AccountStatus.active);
            accountRepository.save(account);
            logger.info("Account status updated: suspended to active");
        }
        if (!account.getStatus().equals(AccountStatus.active) && !revised) {
            logger.warn("This account not is active");
            account.setStatus(AccountStatus.close);
            accountRepository.save(account);
            throw new AccountException("Invalid operation");
        }
        logger.info("Account data was validated");
        return account;
    }

    public void determinatedPenalty(AccountPenalties account) {
        Account bannedAccount = account.getAccount();
        int penaltie_time = switch (account.getBans()) {
            case 0 -> 1;
            case 1 -> 7;
            case 2 -> 8;
            default -> throw new RateLimitExceededException("Invalid data");
        };
        account.setStrikes(account.getStrikes() + 1);
        account.setBans(account.getBans() + 1);
        accountPenaltiesRepository.save(account);
        if (penaltie_time > 7) {
            bannedAccount.setStatus(AccountStatus.close);
            accountRepository.save(bannedAccount);
            throw new RateLimitExceededException( "This account was banned for have maximum number of bans");
        }
        suspendedAccountRepository.save(
            new SuspendedAccount(account.getAccount(), LocalDateTime.now().plusDays((long) penaltie_time))
        );
        bannedAccount.setStatus(AccountStatus.suspended);
        accountRepository.save(bannedAccount);
        throw new RateLimitExceededException("Account is suspended");
    }

    public void penaltiesFree(Account account) {
        redisTemplate.opsForValue().set(
            "penaltie_for_" + account.getId(), 
            "true", 
            Duration.ofMinutes(30)
        );
        accountPenaltiesRepository.save(
            new AccountPenalties(account, 1, 0)
        );
    }

    public void itHasPenalties(AccountPenalties accountPenalties) {
        if ((accountPenalties.getStrikes() + 1) % 3 == 0) {
            determinatedPenalty(accountPenalties);
        }
        boolean isInRedis = redisTemplate.hasKey("penaltie_for_" + accountPenalties.getAccount().getId());
        if (isInRedis) {
            redisTemplate.opsForValue().set(
                "baned_" + accountPenalties.getAccount().getId() + "_24", 
                "true", 
                Duration.ofMinutes(24)
            );
            determinatedPenalty(accountPenalties);
        } else {
            redisTemplate.opsForValue().set(
                "penaltie_for_" + accountPenalties.getAccount().getId(),
                "true", 
                Duration.ofMinutes(30)
            );
            accountPenalties.setStrikes(accountPenalties.getStrikes() + 1);
            accountPenaltiesRepository.save(accountPenalties);
        }
    }

    @Transactional 
    public void validTokenUsage(Account account) {
        List<AuthToken> tokens = authTokenRepository
            .findTop5ByAccountOrderByGeneratedTimeDesc(
                account
        );
        if (tokens.size() < 5) {
            logger.info("Not enough tokens to evaluate usage");
        } else {
            AuthToken lastToken = tokens.get(tokens.size() - 1);
            if (LocalDateTime.now().minusMinutes(5).isBefore(lastToken.getGeneratedTime())) {
                redisTemplate.opsForValue().set(
                    "baned_" + account.getId() + "_5", 
                    "true", 
                    Duration.ofMinutes(5)
                );
                Optional<AccountPenalties> penalties = accountPenaltiesRepository.findByAccount(account);
                if (penalties.isPresent()) {
                    AccountPenalties realPenalties = penalties.get();
                    itHasPenalties(realPenalties);
                } else {
                    penaltiesFree(account);
                }
                throw new RateLimitExceededException("Limit tokens submit");
            }
        }
    }

    public void validAccontInRedis(Long id, int time) {
        boolean isBaned = redisTemplate.hasKey(
            "baned_" + id + "_" + time
        );
        if (isBaned) {
            logger.warn("This account is banned");
            throw new AccountException("Invalid account data");
        }
    }

    public void validJwtInRedis(Long id, String type, String token) {
        String jwtSaved = redisTemplate.opsForValue().get(
            type + "_account_" + id  
        );
        if (jwtSaved == null || !jwtSaved.equals(token)) {
            logger.warn("Token not found or is invalid");
            throw new ValidationFailedException("Invalid token");
        }
        redisTemplate.delete(type + "_account_" + id );
    }
}
