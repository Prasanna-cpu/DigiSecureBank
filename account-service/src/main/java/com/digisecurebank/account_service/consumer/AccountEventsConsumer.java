package com.digisecurebank.account_service.consumer;


import com.digisecurebank.account_service.entity.Account;
import com.digisecurebank.account_service.exceptions.ForbiddenActionException;
import com.digisecurebank.account_service.exceptions.ObjectNotFoundException;
import com.digisecurebank.account_service.repository.AccountRepository;
import com.digisecurebank.account_service.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, ForbiddenActionException.class, ObjectNotFoundException.class})
public class AccountEventsConsumer {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String, Object> payload
    ){
        try{
            String receiverAccountNumber = (String) payload.get("receiverAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            Account account = accountRepository.findByAccountNumber(receiverAccountNumber)
                    .orElseThrow(() -> new ObjectNotFoundException("Account not found " + receiverAccountNumber));

            log.info("Crediting account : {} amount : {}", receiverAccountNumber, amount);
            accountService.creditBalance(receiverAccountNumber, amount, account.getEmail());

        } catch (Exception e) {
            log.error("Error crediting amount : {} " , e.getLocalizedMessage());
        }
    }


    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(
            @Payload Map<String, Object> payload
    ){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            log.info("Blocking account : {}", accountNumber);
            accountService.blockAccount(accountNumber);
        } catch (Exception e) {
            log.error("Error detecting fraud : {} " , e.getLocalizedMessage());
        }
    }

}
