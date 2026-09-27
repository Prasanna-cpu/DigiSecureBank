package com.digisecurebank.auth_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
public class TokenValidator extends OncePerRequestFilter {

    @Value("${jwt.header}")
    private String jwtHeader;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader(jwtHeader != null ? jwtHeader : "Authorization");

        if (jwtSecret == null) {
            throw new BadCredentialsException("JWT secret is not configured");
        }

        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();

                String type = (String) claims.get("type");

                if (!"access".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String email = String.valueOf(claims.getSubject());
                String authorities = String.valueOf(claims.get("authorities"));

                List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorityList);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (MalformedJwtException e) {
                throw new BadCredentialsException("Invalid JWT token: " + e.getMessage());
            } catch (ExpiredJwtException e) {
                throw new BadCredentialsException("Expired JWT token: " + e.getMessage());
            } catch (Exception e) {
                throw new BadCredentialsException("Exception : " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);

    }



}
