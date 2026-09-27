package com.digisecurebank.fraud_detection_service.interceptor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class JWTPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String tokenValue = null;

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            tokenValue = jwtAuthentication.getToken().getTokenValue();
            log.info("Using user JWT token from SecurityContext");
        } else {
            tokenValue = generateServiceToken();
            log.info("Generated service JWT token. Issuer: {}, Subject: fraud-detection-service", jwtIssuer);
        }

        if (tokenValue != null) {
            request.getHeaders().set(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + tokenValue
            );
            log.info("Added Authorization header to request: {}", request.getURI());
        } else {
            log.warn("No token available for request: {}", request.getURI());
        }

        return execution.execute(request, body);
    }

    private String generateServiceToken() {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 3600000); // 1 hour expiration

        return Jwts.builder()
                .issuer(jwtIssuer)
                .subject("fraud-detection-service")
                .claim("type", "access")
                .claim("authorities", "ROLE_ADMIN,ROLE_USER")
                .issuedAt(now)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
