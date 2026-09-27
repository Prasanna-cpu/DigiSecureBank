package com.digisecurebank.account_service.mapper;

import com.digisecurebank.account_service.dto.AccountDTO;
import com.digisecurebank.account_service.entity.Account;

public class AccountMapper {

    public static AccountDTO mapToAccountDTO(Account account){
        return new AccountDTO(account.getId(), account.getAccountNumber(), account.getAccountHolderName(), account.getEmail(), account.getPhone(), account.getAccountType(), account.getBalance(), account.getStatus(), account.getDailyTransactionLimit());
    }

    public static Account mapToAccount(AccountDTO accountDTO){
        Account account = new Account();

        if (accountDTO.getId() != null){
            account.setId(accountDTO.getId());
        }

        if(accountDTO.getAccountNumber() != null){
            account.setAccountNumber(accountDTO.getAccountNumber());
        }

        account.setAccountHolderName(accountDTO.getAccountHolderName());
        account.setEmail(accountDTO.getEmail());
        account.setPhone(accountDTO.getPhone());
        account.setAccountType(accountDTO.getAccountType());
        account.setBalance(accountDTO.getBalance());
        account.setStatus(accountDTO.getStatus());
//        account.setInterest(accountDTO.getInterest());
        account.setDailyTransactionLimit(accountDTO.getDailyTransactionLimit());

        return account;

    }

}
