package com.eripy.core_banking_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eripy.core_banking_system.model.Operation;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByPayer(Long payer);

    Optional<Operation> findById(Long id);
}
