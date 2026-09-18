package com.eripy.core_banking_system.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.core_banking_system.model.Account;

public interface  AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findById(Long id);
}
