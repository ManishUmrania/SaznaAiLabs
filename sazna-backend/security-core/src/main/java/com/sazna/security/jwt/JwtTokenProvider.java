package com.sazna.security.jwt;

import com.sazna.security.util.KeyUtil;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;


public class JwtTokenProvider {

    @Value("${jwt.public-key-path}")
    private  String publicKeyPath;

    @Value("${jwt.private-key-path:}")
    private  String privateKeyPath;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${jwt.expiration:86400000}")
    private  int jwtExpiration;

    @SuppressWarnings("unused")
    @PostConstruct
    public void init() throws Exception {
        if (!(privateKeyPath == null || privateKeyPath.isBlank())) {
            privateKey = KeyUtil.loadPrivateKey(privateKeyPath);
        }
        publicKey = KeyUtil.loadPublicKey(publicKeyPath);
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)                 // setSubject -> subject
                .claim("userId", userId)           // (remains the same)
                .issuedAt(new Date())              // setIssuedAt -> issuedAt
                .expiration(expiryDate)            // setExpiration -> expiration
                .signWith(privateKey)              // JJWT 0.12+ detects RS256 from your RSA PrivateKey
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Handle invalid token exceptions
        }
        return false;
    }
}