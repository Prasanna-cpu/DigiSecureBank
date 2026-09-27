package com.digisecurebank.payment_service.service;


import com.digisecurebank.payment_service.entity.Payment;
import com.digisecurebank.payment_service.enums.PaymentStatus;
import com.digisecurebank.payment_service.exceptions.ForbiddenActionException;
import com.digisecurebank.payment_service.exceptions.ObjectNotFoundException;
import com.digisecurebank.payment_service.repository.PaymentRepository;
import com.digisecurebank.payment_service.response.PaymentOrderResponse;
import com.digisecurebank.payment_service.request.CreatePaymentRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {
        Exception.class
})
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;




    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) throws RazorpayException {
        log.info("Creating Payment Order for Account: {} Amount: {}", request.getAccountNumber(), request.getAmount());
        if(keyId == null || keySecret == null){
            throw new ForbiddenActionException("Razorpay key not found");
        }
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        int convertedAmount = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", convertedAmount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "r_"+ System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 10));

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        log.info("Razorpay order created : {}", razorpayOrder.get("id").toString());

        Payment payment = new Payment();
        payment.setRazorPayOrderId(razorpayOrder.get("id").toString());
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setDescription(request.getDescription());

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentOrderResponse(
                savedPayment.getId().toString(),
                razorpayOrder.get("id").toString(),
                request.getAmount(),
                "INR",
                PaymentStatus.CREATED.toString(),
                keyId
        );


    }

    public void handleWebHook(Map<String, Object> payload){
        log.info("Received Razorpay Webhook : {}", payload.get("event"));
        String event = (String) payload.get("event");

        if("payment.captured".equals(event)){
            handlePaymentSuccess(payload);
        }
        else if("payment.failed".equals(event)){
            handlePaymentFailure(payload);
        }

    }

    private void handlePaymentFailure(Map<String, Object> payload) {
        try{
            Map<String, Object> paymentData = extractPaymentData(payload);
            String orderId = (String) paymentData.get("order_id");

            Payment payment = paymentRepository.findByRazorPayOrderId(orderId)
                    .orElseThrow(() -> new ObjectNotFoundException("Payment Not Found for Order " + orderId));

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment Failed Via RazorPay");
            paymentRepository.save(payment);

            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", payment.getId());
            event.put("accountNumber", payment.getAccountNumber());
            event.put("amount", payment.getAmount());

            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, payment.getId().toString(), event);

        } catch (Exception e) {
            log.error("Error in Processing : {}", e.getLocalizedMessage());
        }
    }

    private void handlePaymentSuccess(Map<String, Object> payload) {

        try{
            Map<String, Object> paymentData = extractPaymentData(payload);
            String orderId = (String) paymentData.get("order_id");
            String paymentId = (String) paymentData.get("id");

            log.info("Extracted paymentId: {}, orderId: {}", paymentId, orderId);

            Payment payment = paymentRepository.findByRazorPayOrderId(orderId)
                    .orElseThrow(() -> new ObjectNotFoundException("Payment Not Found for Order " + orderId));

            payment.setRazorPayPaymentId(paymentId);
            payment.setStatus(PaymentStatus.COMPLETED);
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Saved payment with razorPayPaymentId: {}, status: {}", savedPayment.getRazorPayPaymentId(), savedPayment.getStatus());

            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", payment.getId());
            event.put("accountNumber", payment.getAccountNumber());
            event.put("amount", payment.getAmount());
            event.put("currency", payment.getCurrency());
            event.put("status", payment.getStatus());
            event.put("description", payment.getDescription());

            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, payment.getId().toString() ,event);
            log.info("Payment Completed for Payment ID: {}", payment.getId());

        } catch (Exception e) {
            log.error("Error in Processing : {}", e.getLocalizedMessage());
        }

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPaymentData(Map<String, Object> payload) {
        Map<String, Object> entity = (Map<String, Object>) payload.get("payload");
        Map<String, Object> paymentWrapper = (Map<String, Object>) entity.get("payment");
        return (Map<String, Object>) paymentWrapper.get("entity");
    }


}
