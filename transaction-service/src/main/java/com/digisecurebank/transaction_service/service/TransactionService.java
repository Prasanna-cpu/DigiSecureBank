package com.digisecurebank.transaction_service.service;

import com.digisecurebank.transaction_service.client.AccountServiceClient;
import com.digisecurebank.transaction_service.client.AuthServiceClient;
import com.digisecurebank.transaction_service.dto.AccountDTO;
import com.digisecurebank.transaction_service.dto.TransactionDTO;
import com.digisecurebank.transaction_service.dto.UsersDTO;
import com.digisecurebank.transaction_service.entity.Transaction;
import com.digisecurebank.transaction_service.enums.TransactionStatus;
import com.digisecurebank.transaction_service.enums.TransactionType;
import com.digisecurebank.transaction_service.event.TransactionCompletedEvent;
import com.digisecurebank.transaction_service.event.TransactionInitiatedEvent;
import com.digisecurebank.transaction_service.exceptions.ObjectNotFoundException;
import com.digisecurebank.transaction_service.mapper.TransactionMapper;
import com.digisecurebank.transaction_service.repository.TransactionRepository;
import com.digisecurebank.transaction_service.request.TransferRequest;
import com.digisecurebank.transaction_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {
        Exception.class
})
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final AuthServiceClient authServiceClient;
    private final TransactionValidationService transactionValidationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    public static final String TRANSACTION_REFUNDED_TOPIC = "transcation.refunded";
    public static final String FRAUD_DETECTED_TOPIC = "fraud.detected";

    @Transactional
    public TransactionDTO transfer(TransferRequest request, String jwt){

        ApiResponse userApiResponse = authServiceClient.getUserFromAuthHandler(jwt)
                .orElseThrow(() -> new ObjectNotFoundException("Failed to get user, apiResponse is null"));

        ApiResponse apiResponse = accountServiceClient.getAccountHandler(request.getSenderAccountNumber())
                .orElseThrow(() -> new ObjectNotFoundException("Failed to get account, apiResponse is null"));

        AccountDTO accountDTO = accountServiceClient.extractAccountDTO(Optional.of(apiResponse))
                .orElseThrow(() -> new ObjectNotFoundException("Failed to get account"));

        UsersDTO usersDTO = authServiceClient.extractUserDTO(Optional.of(userApiResponse))
                .orElseThrow(() -> new ObjectNotFoundException("Failed to get user"));

        transactionValidationService.validateTransaction(request, accountDTO);
        transactionValidationService.validateUser(usersDTO.getEmail(), accountDTO, request.getSenderAccountNumber());

        log.info("SAGA START - Transfer : {} -> {} amount : {}", request.getSenderAccountNumber(), request.getReceiverAccountNumber(), request.getAmount());

        accountServiceClient.deductBalanceHandler(
                request.getSenderAccountNumber(),
                request.getAmount()
        );

        Transaction transaction = new Transaction();

        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setReferenceNumber(UUID.randomUUID().toString());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction saved as PROCESSING : {}", savedTransaction.getId());

        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                savedTransaction.getId(),
                savedTransaction.getSenderAccountNumber(),
                savedTransaction.getReceiverAccountNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getDescription()
        );

        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, savedTransaction.getId().toString() ,event);

        return TransactionMapper.mapToTransactionDTO(savedTransaction);
    }

    public TransactionDTO getTransactionById(UUID id){
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found"));
        return TransactionMapper.mapToTransactionDTO(transaction);
    }

    public List<TransactionDTO> getTransactionHistory(String accountNumber){
        return transactionRepository
                .findAllBySenderAccountNumberOrReceiverAccountNumber(accountNumber)
                .stream()
                .map(TransactionMapper::mapToTransactionDTO)
                .toList();
    }

    public TransactionDTO verifyOTP(UUID transactionId, String otp){
        log.info("Verifying OTP for Transaction : {}", transactionId);

        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found"));

        String otpKey = "verification:otp" + transactionId;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if(storedOtp == null){
            log.warn("OTP expired for Transaction : {} ", transactionId);
            compensateTransaction(transaction, "OTP expired");
            return TransactionMapper.mapToTransactionDTO(transaction);
        }

        if(!storedOtp.equals(otp)){
            log.warn("Wrong OTP , blocking account and refunding : {}", transactionId);
            redisTemplate.delete(otpKey);
            blockAccountAndCompensate(transaction, "Wrong OTP received, Account Blocked for Security after Transaction is cancelled");
            return TransactionMapper.mapToTransactionDTO(transaction);
        }

        log.info("OTP Verified , Completing Transaction for: {}", transactionId);
        redisTemplate.delete(otpKey);
        completeTransaction(transaction);
        return TransactionMapper.mapToTransactionDTO(transaction);

    }

    private void completeTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        TransactionCompletedEvent event = new TransactionCompletedEvent(
                transaction.getId(),
                transaction.getSenderAccountNumber(),
                transaction.getReceiverAccountNumber(),
                transaction.getAmount(),
                transaction.getDescription()
        );

        kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, transaction.getId().toString(), event);

        log.info("SAGA Completed, Transaction Completed");
    }

    private void blockAccountAndCompensate(Transaction transaction, String reason) {
        Map<String, Object> fraudEvent = new HashMap<>();

        fraudEvent.put("transactionId", transaction.getId());
        fraudEvent.put("senderAccountNumber", transaction.getSenderAccountNumber());
        fraudEvent.put("reason", reason);

        kafkaTemplate.send(FRAUD_DETECTED_TOPIC,transaction.getSenderAccountNumber(),fraudEvent);

        log.warn("Fraud Detected - account : {}, will be blocked , pls contact the bank", transaction.getSenderAccountNumber());


        compensateTransaction(transaction, reason);

    }

    private void compensateTransaction(Transaction transaction, String reason) {
        log.warn("SAGA Compensation - Refunding: {} amount: {}", transaction.getSenderAccountNumber(), transaction.getAmount());

        accountServiceClient.creditBalanceHandler(
                transaction.getSenderAccountNumber(),
                transaction.getAmount()
        );

        transaction.setStatus(TransactionStatus.FLAGGED);
        transaction.setFailureReason(reason + "SAGA Compensation executed, amount refunded at " + LocalDateTime.now());
        transactionRepository.save(transaction);

        Map<String, Object> refundEvent = new HashMap<>();
        refundEvent.put("transactionId", transaction.getId());
        refundEvent.put("senderAccountNumber", transaction.getSenderAccountNumber());
        refundEvent.put("amount", transaction.getAmount());
        refundEvent.put("reason", reason);
        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC, transaction.getId().toString(), refundEvent);

        log.info("SAGA COMPENSATION COMPLETE , Refunded Transaction: {} to: {} amount: {}", transaction.getId(), transaction.getSenderAccountNumber(), transaction.getAmount());

    }


    public void processCleanResult(UUID transactionId) {
        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found"));

        if(!transaction.getStatus().equals(TransactionStatus.PROCESSING)){
            log.warn("Transaction {} not PROCESSING - skipping", transactionId);
        }

        completeTransaction(transaction);
    }
}
