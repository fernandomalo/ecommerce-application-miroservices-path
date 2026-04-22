package com.fernando.microservices.auth_service.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.fernando.microservices.auth_service.security.service.UserDetailsImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final String jwtSecret = "fkehfkehfk443434sdkdjebfefwrfer34342236676ffgfefr";
    private static final long accessTokenExpiration = 6000000;
    private static final long refreshTokenExpiration = 22200000;

    public String createAccessToken(UserDetailsImpl userDetails) {
        return generateToken(userDetails, accessTokenExpiration);
    }

    public String createRefreshToken(UserDetailsImpl userDetails) {
        return generateToken(userDetails, refreshTokenExpiration);
    }

    public String generateToken(UserDetailsImpl userDetails, long expiration) {
        String email = userDetails.getUsername();
        Long id = userDetails.getId();
        String roles = userDetails.getAuthorities()
                .stream()
                .map(r -> r.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .claim("user_id", id)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + expiration))
                .signWith(key())
                .compact();
    }

    public String getEmailByToken(String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) key())
            .build()
            .parseSignedClaims(token)
            .getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

    public Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
