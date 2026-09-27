package com.digisecurebank.auth_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class JWTProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public static String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> authoritiesSet = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            authoritiesSet.add(authority.getAuthority());
        }
        return String.join(",", authoritiesSet);
    }

    public String generateAccessToken(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = populateAuthorities(authorities);

        String jwt = Jwts
                .builder()
                .issuedAt(new Date())
                .issuer(jwtIssuer)
                .expiration(new Date(new Date().getTime() + 1000 * 60 * 60))
                .subject(authentication.getName()).claim("authorities", roles).claim("type", "access")
                .claim("email", authentication.getName()).signWith(key).compact();

        return jwt;

    }

    public String getEmailFromToken(String jwt) {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
        if (claims.getSubject() != null &&
                !claims.getSubject().isBlank()) {
            return String.valueOf(claims.getSubject());
        } else {
            return String.valueOf(claims.get("email"));
        }
    }

    public String generateRefreshToken(Authentication authentication) {
        return Jwts.builder().issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .subject(authentication.getName()).claim("type", "refresh").claim("email", authentication.getName())
                .signWith(key).compact();
    }

    public Claims extractClaims(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims;
    }


}
