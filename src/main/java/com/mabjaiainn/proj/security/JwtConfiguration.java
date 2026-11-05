package com.mabjaiainn.proj.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
 * A chave de assinatura deve ser fornecida fora do codigo-fonte.
 */
@Configuration
public class JwtConfiguration {

    private final SecretKey key;

    public JwtConfiguration(@Value("${app.security.jwt.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set to a random value of at least 32 characters");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
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
