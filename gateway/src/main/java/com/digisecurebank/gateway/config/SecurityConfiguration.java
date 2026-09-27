package com.digisecurebank.gateway.config;


import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfiguration {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    /**
     * Create the HMAC key used to validate JWTs issued by Auth Service.
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * JWT decoder for Spring Cloud Gateway / WebFlux.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withSecretKey(jwtSecretKey)
                        .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(jwtIssuer);

        OAuth2TokenValidator<Jwt> accessTokenValidator =
                new AccessTokenTypeValidator();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        accessTokenValidator
                )
        );

        return decoder;
    }

    /**
     * Gateway security configuration.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {

        return http
                // Stateless REST API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // CORS preflight
                        .pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Authentication endpoints
                        .pathMatchers("/auth/**")
                        .permitAll()

                        // Health endpoint
                        .pathMatchers("/actuator/health")
                        .permitAll()

                        // Everything else requires authentication
                        .anyExchange()
                        .authenticated()
                )

                // JWT authentication
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                )

                .build();
    }

    /**
     * Only access tokens are accepted by the Gateway.
     *
     * Refresh tokens cannot be used to access protected APIs.
     */
    private static class AccessTokenTypeValidator
            implements OAuth2TokenValidator<Jwt> {

        private static final OAuth2Error INVALID_TOKEN_TYPE =
                new OAuth2Error(
                        "invalid_token",
                        "Token type must be access",
                        null
                );

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {

            String tokenType =
                    jwt.getClaimAsString("type");

            if ("access".equals(tokenType)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    INVALID_TOKEN_TYPE
            );
        }
    }

}
