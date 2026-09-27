package com.digisecurebank.fraud_detection_service.config;


import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }


    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                        .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(jwtIssuer);

        OAuth2TokenValidator<Jwt> accessTokenValidator =
                new AccessTokenTypeValidator();

        decoder.setJwtValidator(
                new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        accessTokenValidator
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String authorities =
                    jwt.getClaimAsString("authorities");
            if (authorities == null || authorities.isBlank()) {
                return Collections.emptyList();
            }
            return Arrays.stream(authorities.split(","))
                    .map(String::trim)
                    .filter(authority -> !authority.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return converter;
    }

    /**
     * Configures endpoint authentication and authorization.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()
                        // Public health endpoint
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // All account endpoints require authentication
                        .requestMatchers(
                                "/api/transactions/**"
                        ).authenticated()
                        // Secure all remaining endpoints by default
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )
                .build();
    }


    private static class AccessTokenTypeValidator
            implements OAuth2TokenValidator<Jwt> {

        private static final OAuth2Error INVALID_TOKEN_TYPE =
                new OAuth2Error(
                        "invalid_token",
                        "Token type must be access",
                        null
                );

        @Override
        public @NonNull OAuth2TokenValidatorResult validate(Jwt jwt) {
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
