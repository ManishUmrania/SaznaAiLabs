package com.sazna.cipher.config;

import com.sazna.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class JwtConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${jwt.public-key-path}") Resource publicKey,
            @Value("${jwt.private-key-path:}") Resource privateKey,
            @Value("${jwt.expiration:86400000}") int expiration
    ) throws Exception {

        return new JwtTokenProvider(publicKey, privateKey, expiration);
    }
}