package com.sazna.gateway.config;

import com.sazna.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("classpath:keys/public.pem") Resource publicKeyResource
    ) throws Exception {

        RSAPublicKey publicKey;

        try (InputStream is = publicKeyResource.getInputStream()) {
            publicKey = (RSAPublicKey) RsaKeyConverters.x509().convert(is);
        }

        return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
    }
}