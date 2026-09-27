package com.digisecurebank.payment_service.interceptor;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTPropagationInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {

            String tokenValue = jwtAuthentication
                    .getToken()
                    .getTokenValue();

            request.getHeaders().set(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + tokenValue
            );
        }

        return execution.execute(request, body);

    }
}
