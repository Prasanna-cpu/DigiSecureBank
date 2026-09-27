package com.digisecurebank.fraud_detection_service.client;



import com.digisecurebank.fraud_detection_service.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @CircuitBreaker(name = "account-service", fallbackMethod = "getBalanceHandlerFallback")
    public Optional<ApiResponse> getBalanceHandler(String accountNumber) {
        var apiResponse = restClient.get()
                .uri("/api/accounts/get-balance/{accountNumber}", accountNumber)
                .retrieve()
                .body(ApiResponse.class);
        log.info("Credit balance response: {}", apiResponse);
        return Optional.ofNullable(apiResponse);
    }

    public void getBalanceHandlerFallback(String accountNumber, Throwable t) {
        log.error("Error getting balance for account number: {}", accountNumber, t);
    }

    public BigDecimal extractBalance(Optional<ApiResponse> apiResponse) {
        return apiResponse
                .map(ApiResponse::getData)
                .map(data -> {
                    return switch (data) {
                        case BigDecimal bigDecimal -> bigDecimal;
                        case Number number -> BigDecimal.valueOf(number.doubleValue());
                        case String s -> new BigDecimal(s);
                        default -> throw new IllegalArgumentException("Unable to convert data to BigDecimal: " + data);
                    };
                })
                .orElseThrow(() -> new IllegalArgumentException("ApiResponse is empty or data is null"));
    }

}
