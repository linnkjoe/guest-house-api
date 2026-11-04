package com.mabjaiainn.proj.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

/**
 * Chave simetrica (HS256) usada para assinar e validar os tokens.
 * Defina JWT_SECRET (minimo 32 caracteres) em producao. Sem ela, e gerada uma
 * chave aleatoria a cada arranque (os tokens deixam de valer ao reiniciar).
 */
@Configuration
public class JwtConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtConfiguration.class);

    private final SecretKey key;

    public JwtConfiguration(@Value("${app.security.jwt.secret:}") String secret) {
        byte[] bytes;
        if (secret == null || secret.isBlank()) {
            log.warn("app.security.jwt.secret (JWT_SECRET) nao definido: a usar chave aleatoria temporaria. "
                    + "Os tokens deixam de ser validos quando a aplicacao reiniciar.");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("app.security.jwt.secret deve ter pelo menos 32 caracteres");
            }
        }
        this.key = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder(AppUserRepository appUserRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> enabledManagerValidator = jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null && roles.contains("GESTOR")
                    && !appUserRepository.existsByUsernameAndEnabledTrue(jwt.getSubject())) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Account is disabled", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(), enabledManagerValidator));
        return decoder;
    }
}
