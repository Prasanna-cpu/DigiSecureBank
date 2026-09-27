package com.digisecurebank.payment_service.controller;


import com.digisecurebank.payment_service.response.ApiResponse;
import com.digisecurebank.payment_service.response.PaymentOrderResponse;
import com.digisecurebank.payment_service.service.PaymentService;
import com.digisecurebank.payment_service.request.CreatePaymentRequest;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/order")
    public ResponseEntity<ApiResponse> createPaymentOrderHandler(
            @Valid @RequestBody CreatePaymentRequest request
    ) throws RazorpayException {
        PaymentOrderResponse response = paymentService.createPaymentOrder(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        response,
                        "Payment Received",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }


    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse> webHookHandler(
            @RequestBody Map<String, Object> payload
    ){
        paymentService.handleWebHook(payload);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        "Webhook Received",
                        "Webhook Received",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }



}
