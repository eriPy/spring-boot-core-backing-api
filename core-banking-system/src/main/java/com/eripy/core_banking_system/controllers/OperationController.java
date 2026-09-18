package com.eripy.core_banking_system.controllers;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.core_banking_system.dto.OperationRequest;
import com.eripy.core_banking_system.dto.OperationResponse;
import com.eripy.core_banking_system.dto.TokenRequest;
import com.eripy.core_banking_system.dto.TokenResponse;
import com.eripy.core_banking_system.model.enums.TokenType;
import com.eripy.core_banking_system.service.OperationService;
import com.eripy.core_banking_system.service.SecurityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
public class OperationController {
    private final SecurityService securityService;
    private final OperationService operationService;

    public OperationController(
        OperationService operationService,
        SecurityService securityService
    ) {
        this.operationService = operationService;
        this.securityService = securityService;
    }

    @GetMapping("/token/{token_type}")
    public TokenResponse getToken(
        Authentication authentication, 
        @PathVariable String token_type
    ) {
        TokenType tokenType = TokenType.valueOf(token_type);
        Long id = Long.parseLong(authentication.getName());
        securityService.validAccontInRedis(id, 24); 
        return operationService.getSixDigitToken(id, tokenType);
    }

    @PostMapping("/token/{operation_type}")
    public OperationResponse getJwtToken(
        Authentication authentication, 
        @PathVariable 
        String operation_type,
        @Valid @RequestBody 
        TokenRequest tokenRequest
    ) {
        TokenType operationType = TokenType.valueOf(operation_type);
        Long id = Long.parseLong(authentication.getName());
        securityService.validAccontInRedis(id, 24); 
        return operationService.getOperaionJwt(id, tokenRequest, operationType);
    }

    @PostMapping("/transaction")
    public Map<String, String> proccessTransaction(
        Authentication authentication, 
        @Valid @RequestBody OperationRequest operationRequest
    ) {
        Long id = Long.parseLong(authentication.getName());
        securityService.validAccontInRedis(id, 24);
        operationService.processTransaction(id, operationRequest); 
        return Map.of(
            "Detail", "Operation was success"
        );
    }
}
