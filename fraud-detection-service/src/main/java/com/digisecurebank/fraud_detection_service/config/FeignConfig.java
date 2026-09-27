package com.digisecurebank.fraud_detection_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignConfig implements RequestInterceptor {

    @Value("${account-service.jwt-token:}")
    private String jwtToken;

    @Override
    public void apply(RequestTemplate template) {
        if (jwtToken != null && !jwtToken.isEmpty()) {
            template.header("Authorization", "Bearer " + jwtToken);
        }
    }
}
