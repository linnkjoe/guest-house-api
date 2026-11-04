package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.LoginRequest;
import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.DTO.AuthUserDetails;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Duration tokenValidity;
    // Hash falso: permite gastar o mesmo tempo quando o utilizador nao existe.
    private final String dummyHash;

    public AuthService(AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expiration-minutes:480}") long expirationMinutes) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.tokenValidity = Duration.ofMinutes(expirationMinutes);
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        AuthUserDetails user = appUserRepository.findAuthDetailsByUsername(username).orElse(null);

        String hash = user != null ? user.passwordHash() : dummyHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (user == null || !user.enabled() || !passwordMatches) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        Long clientId = user.clientId();

        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("mabjaia-inn-api")
                .subject(user.username())
                .issuedAt(now)
                .expiresAt(now.plus(tokenValidity))
                .claim("roles", List.of(user.role().name()))
                .claim("mustChangePassword", user.mustChangePassword());
        if (clientId != null) {
            claims.claim("clientId", clientId);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();

        String name = user.displayName() != null && !user.displayName().isBlank()
                ? user.displayName() : user.username();

        return new LoginResponse(token, "Bearer", tokenValidity.toSeconds(),
                user.username(), name, user.role().name(), clientId, user.mustChangePassword());
    }
}
