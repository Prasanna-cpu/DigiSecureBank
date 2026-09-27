package com.digisecurebank.account_service.service;


import com.digisecurebank.account_service.client.AuthServiceClient;
import com.digisecurebank.account_service.dto.AccountDTO;
import com.digisecurebank.account_service.dto.UsersDTO;
import com.digisecurebank.account_service.entity.Account;
import com.digisecurebank.account_service.enums.AccountStatus;
import com.digisecurebank.account_service.enums.AccountType;
import com.digisecurebank.account_service.exceptions.*;
import com.digisecurebank.account_service.mapper.AccountMapper;
import com.digisecurebank.account_service.repository.AccountRepository;
import com.digisecurebank.account_service.request.CreateAccountRequest;
import com.digisecurebank.account_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {
        Exception.class,
        ObjectNotFoundException.class,
        AccountNotActiveException.class,
        InsufficientBalanceException.class,
        ConflictingResourcesException.class,
        ForbiddenActionException.class
})
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthServiceClient authServiceClient;
    private final AccountValidationService accountValidationService;


    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);

            accountNumber = String.format("%012d", number);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional
    public AccountDTO createAccount(CreateAccountRequest request, String token){

        ApiResponse apiResponse = authServiceClient.getUserFromAuthHandler(token).orElseThrow(
                () -> new ObjectNotFoundException("User Not Found , as Api Response is empty")
        );

        UsersDTO usersDTO = authServiceClient.extractUserDTO(Optional.of(apiResponse))
                        .orElseThrow(() -> new ObjectNotFoundException("User not found"));

        accountValidationService.validateUserForAccountCreation(usersDTO, request.getAccountHolderName(), request.getEmail());

        log.info("Creating Account For : {} ", request.getEmail());

        if(accountRepository.existsByEmail(request.getEmail())){
            throw new ConflictingResourcesException("Given Account Already Exists");
        }

        Account account = new Account();

        account.setAccountHolderName(request.getAccountHolderName());
        account.setAccountType(request.getAccountType());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setDailyTransactionLimit(
                request.getAccountType().equals(AccountType.SAVINGS) ? new BigDecimal(100000) : new BigDecimal(500000)
        );
        account.setAccountNumber(generateAccountNumber());

        Account savedAccount = accountRepository.save(account);
        AccountDTO savedAccountDTO = AccountMapper.mapToAccountDTO(savedAccount);

        return savedAccountDTO;

    }

    public AccountDTO getAccountById(UUID id){
        Account account = accountRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("Account Not Found With The Id : " + id)
                );

        return AccountMapper.mapToAccountDTO(account);
    }

    public AccountDTO getAccount(String accountNumber){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new ObjectNotFoundException("Account Not Found With The Account Number : " + accountNumber)
                );
        return AccountMapper.mapToAccountDTO(account);
    }

    @Transactional
    public void blockAccount(String accountNumber){
        log.info("Blocking account : {}", accountNumber);
        AccountDTO accountDTO = getAccount(accountNumber);
        Account account = AccountMapper.mapToAccount(accountDTO);
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    public BigDecimal getBalance(String accountNumber) {
//        accountValidationService.validateUserWithAccNo(accountNumber, email);
        return getAccount(accountNumber).getBalance();
    }

    @Transactional
    public void deductBalance(String accountNumber, BigDecimal amount, String email) {
        accountValidationService.validateUserWithAccNo(accountNumber, email);
        log.info("Deducting {} from account {}", amount, accountNumber);
        AccountDTO accountDTO = getAccount(accountNumber);
        Account account = AccountMapper.mapToAccount(accountDTO);

        if(!account.getStatus().equals(AccountStatus.ACTIVE)){
            throw new AccountNotActiveException("Account is not active , so the following Transaction is Forbidden");
        }

        if(amount.compareTo(account.getBalance()) > 0){
            throw new InsufficientBalanceException("Insufficient Balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void creditBalance(String accountNumber, BigDecimal amount, String email) {
        accountValidationService.validateUserWithAccNo(accountNumber, email);
        log.info("Crediting {} to account {}", amount, accountNumber);
        AccountDTO accountDTO = getAccount(accountNumber);
        Account account = AccountMapper.mapToAccount(accountDTO);

        if(!account.getStatus().equals(AccountStatus.ACTIVE)){
            throw new AccountNotActiveException("Account is not active , so the following Transaction is Forbidden");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    public AccountDTO getAccountByEmail(String email){
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found"));

        return AccountMapper.mapToAccountDTO(account);
    }


}
