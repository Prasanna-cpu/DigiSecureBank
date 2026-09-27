package com.digisecurebank.account_service.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AuthServiceClientConfiguration {

    @Value("${auth-service.url}")
    private String authServiceUrl;

    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl(authServiceUrl).build();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}

