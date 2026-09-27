package com.digisecurebank.transaction_service.client;


import com.digisecurebank.transaction_service.dto.AccountDTO;
import com.digisecurebank.transaction_service.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class AccountServiceClient {

    @Qualifier("restClientAccount")
    private final RestClient restClient;

    @Qualifier("objectMapperAccount")
    private final ObjectMapper objectMapper;


    public AccountServiceClient(@Qualifier("restClientAccount") RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountHandlerFallback")
    @Retry(name = "account-service")
    public Optional<ApiResponse> getAccountHandler(String accountNumber) {
//        String authorizationHeader = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
        var apiResponse = restClient.get()
                .uri("/api/accounts/getByAccountNumber/{accountNumber}", accountNumber)
//                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .retrieve()
                .body(ApiResponse.class);
        log.info("Get account response: {}", apiResponse);
        return Optional.ofNullable(apiResponse);
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "deductBalanceHandlerFallback")
//    @Retry(name = "account-service")
    public Optional<ApiResponse> deductBalanceHandler(String accountNumber, BigDecimal amount) {
//        String authorizationHeader = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
        var apiResponse = restClient.put()
                .uri("/api/accounts/deduct/{accountNumber}?amount={amount}", accountNumber, amount)
//                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .retrieve()
                .body(ApiResponse.class);
        log.info("Deduct balance response: {}", apiResponse);
        return Optional.ofNullable(apiResponse);
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "creditBalanceHandlerFallback")
    public Optional<ApiResponse> creditBalanceHandler(String accountNumber, BigDecimal amount) {
        var apiResponse = restClient.put()
                .uri("/api/accounts/credit/{accountNumber}?amount={amount}", accountNumber, amount)
                .retrieve()
                .body(ApiResponse.class);
        log.info("Credit balance response: {}", apiResponse);
        return Optional.ofNullable(apiResponse);
    }

    Optional<ApiResponse> deductBalanceHandlerFallback(String accountNumber, BigDecimal amount, Exception e) {
        System.out.println("Exception : " + e.getMessage());
        return Optional.empty();
    }

    Optional<ApiResponse> getAccountHandlerFallback(String accountNumber, Exception e) {
        System.out.println("Exception : " + e.getMessage());
        return Optional.empty();
    }

    Optional<ApiResponse> creditBalanceHandlerFallback(String accountNumber, BigDecimal amount, Exception e) {
        System.out.println("Exception : " + e.getMessage());
        return Optional.empty();
    }

    public Optional<AccountDTO> extractAccountDTO(Optional<ApiResponse> response) {
        return response.map(ApiResponse::getData).map(data -> objectMapper.convertValue(data, AccountDTO.class));
    }
}
