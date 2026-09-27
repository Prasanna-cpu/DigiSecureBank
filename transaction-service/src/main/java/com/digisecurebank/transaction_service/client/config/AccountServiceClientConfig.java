package com.digisecurebank.transaction_service.client.config;


import com.digisecurebank.transaction_service.interceptor.JWTPropagationInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class AccountServiceClientConfig {

    private final JWTPropagationInterceptor jwtPropagationInterceptor;

    @Value("${account-service.url}")
    private String accountServiceUrl;



    @Bean
    RestClient restClientAccount() {
        return RestClient
                .builder()
                .baseUrl(accountServiceUrl)
                .requestInterceptor(jwtPropagationInterceptor)
                .build();
    }

    @Bean
    ObjectMapper objectMapperAccount() {
        return new ObjectMapper();
    }
}
