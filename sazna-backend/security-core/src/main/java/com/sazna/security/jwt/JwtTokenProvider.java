package com.sazna.security.jwt;

import com.sazna.security.util.KeyUtil;
import io.jsonwebtoken.*;
import org.springframework.core.io.Resource;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

public class JwtTokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final int jwtExpiration;

    public JwtTokenProvider(Resource publicKeyResource,
                            Resource privateKeyResource,
                            int jwtExpiration) throws Exception {

        if (privateKeyResource != null) {
            this.privateKey = KeyUtil.loadPrivateKey(privateKeyResource);
        } else {
            this.privateKey = null;
        }

        this.publicKey = KeyUtil.loadPublicKey(publicKeyResource);
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}