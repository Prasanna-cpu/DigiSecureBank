package com.digisecurebank.transaction_service.service;


import com.digisecurebank.transaction_service.dto.AccountDTO;
import com.digisecurebank.transaction_service.dto.UsersDTO;
import com.digisecurebank.transaction_service.exceptions.ForbiddenActionException;
import com.digisecurebank.transaction_service.exceptions.InsufficientBalanceException;
import com.digisecurebank.transaction_service.request.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionValidationService {

    public void validateUser(String email, AccountDTO accountDTO, String senderAccountNumber){
        // Validate that email associated with the account , has the accountNumber , Matching with senderAccountNumber

        if(!email.equals(accountDTO.getEmail())){
            throw new ForbiddenActionException("Sender Email does not match");
        }

        if(!senderAccountNumber.equals(accountDTO.getAccountNumber())){
            throw new ForbiddenActionException("Sender account number does not match");
        }


    }


    public void validateTransaction(TransferRequest request, AccountDTO accountDTO) {

        if (!request.getSenderAccountNumber().equals(accountDTO.getAccountNumber())) {
            throw new ForbiddenActionException("Sender account number does not match");
        }

        if (request.getAmount().compareTo(accountDTO.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

    }

}
