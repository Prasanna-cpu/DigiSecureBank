package com.digisecurebank.account_service.service;


import com.digisecurebank.account_service.client.AuthServiceClient;
import com.digisecurebank.account_service.dto.UsersDTO;
import com.digisecurebank.account_service.entity.Account;
import com.digisecurebank.account_service.exceptions.ForbiddenActionException;
import com.digisecurebank.account_service.exceptions.ObjectNotFoundException;
import com.digisecurebank.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, ForbiddenActionException.class})
public class AccountValidationService {

    private final AuthServiceClient authServiceClient;
    private final AccountRepository accountRepository;

    public void validateUserForAccountCreation(UsersDTO usersDTO, String accountHolderName, String email){
        if(!usersDTO.getFullName().equals(accountHolderName) || !usersDTO.getEmail().equals(email)){
            throw new ForbiddenActionException("User is not authorized to create account");
        }
    }

    public void validateUserWithAccNo(String accountNumber, String email){
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Account not found " + accountNumber));

        if(!email.equals(account.getEmail())){
            throw new ForbiddenActionException("User is not authorized to perform this action");
        }
    }

}
