package com.digisecurebank.account_service.client;

import com.digisecurebank.account_service.dto.UsersDTO;
import com.digisecurebank.account_service.response.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;


    @CircuitBreaker(name = "auth-service", fallbackMethod = "getUserFromAuthHandlerFallback")
    @Retry(name = "auth-service")
    public Optional<ApiResponse> getUserFromAuthHandler(String jwt){
        String authorizationHeader = jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
        var apiResponse = restClient.get().uri("/api/users/me")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader)).retrieve()
                .body(ApiResponse.class);
        log.info("apiResponse of User : {}", apiResponse);
        return Optional.ofNullable(apiResponse);
    }

    Optional<ApiResponse> getUserFromAuthHandlerFallback(String jwt, Exception e) {
        System.out.println("Exception : " + e.getMessage());
        return Optional.empty();
    }

    public Optional<UsersDTO> extractUserDTO(Optional<ApiResponse> response) {
        return response.map(ApiResponse::getData).map(data -> objectMapper.convertValue(data, UsersDTO.class));
    }






}
