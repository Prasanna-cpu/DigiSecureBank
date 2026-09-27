package com.digisecurebank.account_service.controller;


import com.digisecurebank.account_service.dto.AccountDTO;
import com.digisecurebank.account_service.request.CreateAccountRequest;
import com.digisecurebank.account_service.response.ApiResponse;
import com.digisecurebank.account_service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAccountHandler(
            @Valid @RequestBody CreateAccountRequest request,
            @RequestHeader("Authorization") String token
    ){
        AccountDTO accountDTO = accountService.createAccount(request, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse(
                        accountDTO,
                        "Account Created",
                        HttpStatus.CREATED,
                        HttpStatus.CREATED.value()
                )
        );
    }

    @GetMapping("/get-balance/{accountNumber}")
    public ResponseEntity<ApiResponse> getBalanceHandler(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt
    ){
        String email = jwt.getSubject();
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        balance,
                        "Balance Retrieved",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/getByEmail")
    public ResponseEntity<ApiResponse> getAccountByEmailHandler(
            @AuthenticationPrincipal Jwt jwt
    ){
        String email = jwt.getSubject();
        AccountDTO accountDTO = accountService.getAccountByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        accountDTO,
                        "Account Retrieved",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/getById/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> getAccountByIdHandler(@PathVariable UUID id){
        AccountDTO accountDTO = accountService.getAccountById(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        accountDTO,
                        "Account Found",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/getByAccountNumber/{accountNumber}")
    public ResponseEntity<ApiResponse> getAccountHandler(@PathVariable String accountNumber){
        AccountDTO accountDTO = accountService.getAccount(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        accountDTO,
                        "Account Found",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @PutMapping("/blockAccount/{accountNumber}")
    public ResponseEntity<ApiResponse> blockAccountHandler(@PathVariable String accountNumber){
        accountService.blockAccount(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        null,
                        "Account Blocked",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @PutMapping("/credit/{accountNumber}")
    public ResponseEntity<ApiResponse> creditBalanceHandler(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal Jwt jwt
    ){
        String email = jwt.getSubject();
        accountService.creditBalance(accountNumber, amount, email);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        null,
                        "Amount Credited",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @PutMapping("/deduct/{accountNumber}")
    public ResponseEntity<ApiResponse> deductBalanceHandler(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal Jwt jwt
    ){
        String email = jwt.getSubject();
        accountService.deductBalance(accountNumber, amount, email);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        null,
                        "Amount Deducted",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }


}
