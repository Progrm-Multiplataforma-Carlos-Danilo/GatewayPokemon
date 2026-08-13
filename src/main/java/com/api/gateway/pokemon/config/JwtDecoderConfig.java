package com.api.gateway.pokemon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtDecoderConfig {
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        final String secretKey = "YmmvlRZIh7Ev4xW9k9MTrdCi2IbY4giwA122EjUZ9UYmLq6cURIo2Ty0CFBPemhe";
        return NimbusReactiveJwtDecoder.withSecretKey(
                new SecretKeySpec(secretKey.getBytes(), "HmacSHA256")
        ).build();
    }
}