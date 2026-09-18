package com.eripy.core_banking_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.SuspendedAccount;

public interface SuspendedAccountRepository extends JpaRepository<SuspendedAccount, Long> {
    Optional<SuspendedAccount> findByAccount(Account account);
}
