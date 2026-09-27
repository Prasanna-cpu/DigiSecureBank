package com.digisecurebank.fraud_detection_service.service;


import com.digisecurebank.fraud_detection_service.client.AccountServiceClient;
import com.digisecurebank.fraud_detection_service.dto.FraudCheckResult;
import com.digisecurebank.fraud_detection_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = {
        Exception.class,
        Error.class,
        RuntimeException.class
})
public class FraudDetectionService {

    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${fraud.max-transactions-per-minute}")
    private int maxTransactionsPerMinute;

    @Value("${fraud.multiplier}")
    private int multiplier;

    @Value("${fraud.percentage}")
    private float percentage;

    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN = "fraud.check.clean";


    public void checkTransaction(Map<String, Object> payload) {
        UUID transactionId = UUID.fromString(payload.get("transactionId").toString());
        String accountNumber = (String) payload.get("senderAccountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        ApiResponse apiResponse = accountServiceClient.getBalanceHandler(accountNumber).orElseThrow(
                () -> new RuntimeException("Failed to get balance for account number: " + accountNumber)
        );

        BigDecimal senderBalance = new BigDecimal(apiResponse.getData().toString());

        log.info("Creating transaction : {} account: {} amount: {} balance: {}",
                transactionId, accountNumber, amount, senderBalance);

        FraudCheckResult result = performFraudChecks(accountNumber, amount, senderBalance);

        if(result.getFraud()){
            log.info("Suspicious Activity Detected - account: {} reason: {} requesting OTP verification", accountNumber,result.getReason());

            Map<String, Object> verificationEvent = new HashMap<>();
            verificationEvent.put("transactionId", transactionId);
            verificationEvent.put("accountNumber", accountNumber);
            verificationEvent.put("amount", amount);
            verificationEvent.put("reason", result.getReason());
            verificationEvent.put("status", "PENDING");

            kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, String.valueOf(transactionId),verificationEvent);

        }
        else{
            log.info("Transaction clean");
            log.info("Transaction : {} account: {} amount: {} balance: {}",
                    transactionId, accountNumber, amount, senderBalance);

            Map<String, Object> transactionEvent = new HashMap<>();
            transactionEvent.put("transactionId", transactionId);
            transactionEvent.put("isFraud", false);
            transactionEvent.put("reason", null);


            kafkaTemplate.send(FRAUD_CHECK_CLEAN, String.valueOf(transactionId),transactionEvent);
        }

    }

    private FraudCheckResult performFraudChecks(String accountNumber, BigDecimal amount, BigDecimal senderBalance) {
        if(isTransactionVelocityExceeded(accountNumber, amount, senderBalance)){
            return new FraudCheckResult(true, "Too many transactions in 60s - Limit Exceeded");
        }

        if(isAmountSuspicious(accountNumber, amount)){
            return new FraudCheckResult(
                    true,
                    "Unusual Transaction Detected , Exceeded your current daily average by 3 times"
            );
        }

        if(senderBalance.compareTo(BigDecimal.ZERO) > 0 && isBalanceCheckFailed(senderBalance, amount)){
            return new FraudCheckResult(true, "Transaction Exceeds 90 percent of your Balance");
        }

        return new FraudCheckResult(false, null);
    }

    private boolean isBalanceCheckFailed(BigDecimal senderBalance, BigDecimal amount) {
        BigDecimal maxAllowed = senderBalance.multiply(
                BigDecimal.valueOf(percentage));

        log.info("Balance check - amount: {} maxAllowed: {} suspicious: {}",
                amount, maxAllowed, amount.compareTo(maxAllowed) > 0);

        return amount.compareTo(maxAllowed) > 0;
    }

    private boolean isAmountSuspicious(String accountNumber, BigDecimal amount) {
        String avgKey = "fraud:avg-amount" + accountNumber;
        String avgStr = redisTemplate.opsForValue().get(avgKey);

        if(avgStr == null){
            redisTemplate.opsForValue().set(avgKey, amount.toString());
            return false;
        }

        BigDecimal avg = new BigDecimal(avgStr);
        BigDecimal threshold = avg.multiply(new BigDecimal(multiplier));
        BigDecimal newAvg= avg.add(amount).divide(new BigDecimal(2),2, RoundingMode.HALF_UP);
        redisTemplate.opsForValue().set(avgKey, newAvg.toString());

        log.info("Amount Suspicious - account: {} amount: {} avg: {} threshold: {} suspicious: {}", accountNumber, amount, avg, threshold, amount.compareTo(threshold) > 0);

        return amount.compareTo(threshold) > 0;
    }

    private boolean isTransactionVelocityExceeded(String accountNumber, BigDecimal amount, BigDecimal senderBalance) {
        String key = "fraud:velocity" + accountNumber;
        Long count = redisTemplate.opsForValue().increment(key, 1);

        if(count != null && count == 1){
            redisTemplate.expire(key, Duration.ofSeconds(60));
        }

        log.info("Velocity Check - account : {} count: {}/{}", accountNumber, count, maxTransactionsPerMinute);
        return count != null && count > maxTransactionsPerMinute;
    }



}
