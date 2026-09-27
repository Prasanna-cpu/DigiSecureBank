package com.digisecurebank.transaction_service.controller;


import com.digisecurebank.transaction_service.dto.TransactionDTO;
import com.digisecurebank.transaction_service.request.TransferRequest;
import com.digisecurebank.transaction_service.response.ApiResponse;
import com.digisecurebank.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse> transferHandler(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){

        TransactionDTO transactionDTO = transactionService.transfer(request, jwt.getTokenValue());
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        transactionDTO,
                        "Money Transferred",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<ApiResponse> getTransactionHistoryHandler(@PathVariable String accountNumber){
        List<TransactionDTO> transactionsDTO = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        transactionsDTO,
                        "Transaction History",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );

    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse> getTransactionByIdHandler(@PathVariable UUID transactionId){
        TransactionDTO transactionDTO = transactionService.getTransactionById(transactionId);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        transactionDTO,
                        "Transaction Retrieved",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

    @GetMapping("/verify/{transactionId}/{otp}")
    public ResponseEntity<ApiResponse> verifyTransaction(
            @PathVariable UUID transactionId,
            @PathVariable String otp
    ){
        TransactionDTO transactionDTO = transactionService.verifyOTP(transactionId,otp);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        transactionDTO,
                        "OTP Verification",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }



}
