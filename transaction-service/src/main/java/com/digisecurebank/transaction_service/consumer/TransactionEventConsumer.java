package com.digisecurebank.transaction_service.consumer;

import com.digisecurebank.transaction_service.entity.Transaction;
import com.digisecurebank.transaction_service.enums.TransactionStatus;
import com.digisecurebank.transaction_service.exceptions.ObjectNotFoundException;
import com.digisecurebank.transaction_service.repository.TransactionRepository;
import com.digisecurebank.transaction_service.service.TransactionService;
import jakarta.persistence.TransactionRequiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
@Transactional(rollbackFor = {
        Exception.class,
        RuntimeException.class,
        ObjectNotFoundException.class
})
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionService transactionService;



    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final String TRANSACTION_OTP_GENERATED_TOPIC = "transaction.otp.generated";

    @KafkaListener(topics = "verification.required")
    public void consumerVerificationRequired(
            @Payload Map<String, Object> payload
    ){

        try{
            UUID transactionId = UUID.fromString(payload.get("transactionId").toString());
            String accountNumber = (String) payload.get("accountNumber");
            String reason = (String) payload.get("reason");

            log.info("Verification - Required Transaction: {} , Reason: {} ", transactionId, reason);

            Transaction transaction = transactionRepository
                    .findById(transactionId)
                    .orElseThrow(() -> new ObjectNotFoundException("Transaction No Found " + transactionId));


            if(!transaction.getStatus().equals(TransactionStatus.PENDING)){
                log.warn("Transaction {} not processing - skipping", transactionId);
            }

            String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);
            String otpKey = "verification:otp" + transactionId;
            redisTemplate.opsForValue().set(otpKey, otp);
            redisTemplate.expire(otpKey, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

            transaction.setStatus(TransactionStatus.PENDING_VERIFICATION);
            transactionRepository.save(transaction);

            log.info("OTP generated for Transaction : {} expires in {} minutes", transactionId, OTP_EXPIRY_MINUTES);

            Map<String, Object> otpEvent = new HashMap<>();
            otpEvent.put("transactionId", transactionId);
            otpEvent.put("otp", otp);
            otpEvent.put("accountNumber", accountNumber);
            otpEvent.put("reason", reason);
            otpEvent.put("amount", payload.get("amount"));

            kafkaTemplate.send(TRANSACTION_OTP_GENERATED_TOPIC, transactionId.toString(), otpEvent);


        }
        catch (Exception e){
            log.error("Error Handling Verification Required : {}", e.getLocalizedMessage());
        }

    }

    @KafkaListener(topics = "fraud.check.clean")
    public void consumeFraudCheckCleanResult(
            @Payload Map<String, Object> payload

    ){
        try{
            UUID transactionId = UUID.fromString(payload.get("transactionId").toString());
            transactionService.processCleanResult(transactionId);


        } catch (Exception e) {
            log.error("Error Handling Fraud Check Clean Result : {}", e.getLocalizedMessage());
        }
    }



}
