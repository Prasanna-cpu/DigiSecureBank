package com.digisecurebank.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional(rollbackFor = {
        Exception.class,
        RuntimeException.class,
        Error.class
})
public class NotificationService {

    @KafkaListener(topics = "transaction.otp.generated")
    public void consumeOTPGenerated(
            @Payload Map<String, Object> payload
    ){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            String otp = (String) payload.get("otp");
            UUID transactionId = (UUID) payload.get("transactionId");
            String amount = payload.get("amount").toString();
            String reason = (String) payload.get("reason");

            sendAlert(accountNumber,"TRANSACTION VERIFICATION REQUIRED",
                    String.format("Suspicious transaction of %s %s , OTP is: %s, valid for 5 minutes", amount, reason, otp)
            );

        }
        catch (Exception e){
            log.error("Error sending OTP notification generated", e);
        }
    }

    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String, Object> payload
    ){
        try{
            String senderAccount = (String) payload.get("senderAccount");
            String receiverAccount = (String) payload.get("receiverAccount");
            String amount = payload.get("amount").toString();

            sendAlert(senderAccount, "DEBIT ALERT", String.format("Debit of %s from account %s", amount, senderAccount));
            sendAlert(receiverAccount, "CREDIT ALERT", String.format("Credit of %s to account %s", amount, receiverAccount));
        }
        catch (Exception e){
            log.error("Error sending transaction completed notification", e);
        }
    }

    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(
            @Payload Map<String, Object> payload
    ){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            String reason = (String) payload.get("reason");

            sendAlert(accountNumber, "FRAUD ALERT", String.format("Your account %s has been blocked . Reason %s , Please contact the bank", accountNumber, reason));
        }
        catch (Exception e) {
            log.error("Error sending fraud detected notification", e);
        }
    }

    @KafkaListener(topics = "transaction.refunded")
    public void consumeTransactionRefunded(
            @Payload Map<String, Object> payload
    ){
        try{

            String senderAccount = (String) payload.get("senderAccount");
//            String receiverAccount = (String) payload.get("receiverAccount");
            String amount = payload.get("amount").toString();
            String reason = (String) payload.get("reason");

            sendAlert(senderAccount, "REFUND PROCESSED", String.format("Your transaction of %s was cancelled, Amount of %s would be refunded to %s, Reason %s", amount, amount, senderAccount, reason));


        } catch (Exception e) {
            log.error("Error sending transaction refunded notification", e);
        }
    }

    @KafkaListener(topics = "payment.completed")
    public void consumePaymentCompleted(
            @Payload Map<String, Object> payload
    ){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            String amount = payload.get("amount").toString();

            sendAlert(accountNumber, "PAYMENT COMPLETED", String.format("Your payment of %s was successful", amount));

        } catch (Exception e) {
            log.error("Error sending payment completed notification", e);
        }
    }

    @KafkaListener(topics = "payment.failed")
    public void consumePaymentFailed(
            @Payload Map<String, Object> payload
    ){
        try{
            String accountNumber = (String) payload.get("accountNumber");
            String amount = payload.get("amount").toString();

            sendAlert(accountNumber, "PAYMENT FAILED", String.format("Your payment of %s failed", amount));
        }
        catch (Exception e) {
            log.error("Error sending payment failed notification", e);
        }

    }



    private void sendAlert(String accountNumber, String subject, String message) {
        log.info("_________________________________________________");
        log.info("Account Number: {}", accountNumber);
        log.info("Subject: {}", subject);
        log.info("Message: {}", message);
        log.info("_________________________________________________");



    }

}
