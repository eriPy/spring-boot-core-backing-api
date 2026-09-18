package com.eripy.core_banking_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.core_banking_system.model.Account;
import com.eripy.core_banking_system.model.AccountPenalties;

public interface AccountPenaltiesRepository extends JpaRepository<AccountPenalties, Long> {
    Optional<AccountPenalties> findByAccount(Account account);
}
