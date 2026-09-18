package com.eripy.core_banking_system.controllers;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.core_banking_system.dto.IdRequest;
import com.eripy.core_banking_system.dto.TokenResponse;
import com.eripy.core_banking_system.dto.TokenToDisableRequest;
import com.eripy.core_banking_system.model.Operation;
import com.eripy.core_banking_system.service.AccountService;
import com.eripy.core_banking_system.service.SecurityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/me")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;
    private final SecurityService securityService;
    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

    public AccountController(
        AccountService accountService,
        SecurityService securityService
    ) {
        this.accountService = accountService;
        this.securityService = securityService;
    }

    @GetMapping("/auth/{id}")
    public TokenResponse getJwtHeader(@PathVariable Long id) {
        IdRequest idRequest = new IdRequest(id);
        logger.info("Searching user to return a JWT token");
        return accountService.verifyAccount(idRequest);
    }

    @GetMapping("/transactions/{id}")
    public List<Operation> getMyTransactions(@PathVariable Long id) {
        IdRequest idRequest = new IdRequest(id);
        securityService.validAccontInRedis(idRequest.id(), 24);
        return accountService.transactionsFind(idRequest);
    }

    @PatchMapping ("/disable")
    public Map<String, String> disableAccount(
        Authentication authentication, 
        @Valid @RequestBody TokenToDisableRequest toDisableRequest
    ) {
        Long id = Long.parseLong(authentication.getName());
        securityService.validAccontInRedis(id, 24); 
        accountService.disableAccount(id, toDisableRequest);
        return Map.of(
            "Detail", "Account was disable"
        );
    }

    @GetMapping("/transaction/{transactionId}")
    public Operation getTrasactionDescription(
        Authentication authentication, 
        @PathVariable Long transactionId
    ) {
        IdRequest idRequest = new IdRequest(transactionId);
        Long id = Long.parseLong(authentication.getName());
        securityService.validAccontInRedis(id, 24); 
        return accountService.findTransaction(idRequest);
    }
}
