package com.mabjaiainn.proj.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabjaiainn.proj.DTO.GoogleAuthResponse;
import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final Duration COMPLETION_CODE_VALIDITY = Duration.ofMinutes(5);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String configuredClientId;
    private final String configuredClientSecret;
    private final String credentialsFile;
    private final String redirectUri;
    private final ClientService clientService;
    private final AuthService authService;
    private final AppUserRepository appUserRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PendingIdentity> pendingIdentities = new ConcurrentHashMap<>();
    private volatile GoogleCredentials cachedCredentials;

    public GoogleAuthService(
            @Value("${app.security.google.client-id:}") String clientId,
            @Value("${app.security.google.client-secret:}") String clientSecret,
            @Value("${app.security.google.credentials-file:}") String credentialsFile,
            @Value("${app.security.google.redirect-uri:http://localhost:5173/backend/auth/google/callback}") String redirectUri,
            ClientService clientService,
            AuthService authService,
            AppUserRepository appUserRepository) {
        this.configuredClientId = clientId == null ? "" : clientId.trim();
        this.configuredClientSecret = clientSecret == null ? "" : clientSecret.trim();
        this.credentialsFile = credentialsFile == null ? "" : credentialsFile.trim();
        this.redirectUri = redirectUri.trim();
        this.clientService = clientService;
        this.authService = authService;
        this.appUserRepository = appUserRepository;
        this.restClient = RestClient.create();
    }

    public String authorizationUrl(String state, String nonce) throws BusinessException {
        GoogleCredentials credentials = credentials();
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(credentials.clientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&nonce=" + encode(nonce)
                + "&prompt=select_account";
    }

    public String completeAuthorization(String authorizationCode, String expectedNonce) throws BusinessException {
        GoogleCredentials credentials = credentials();
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", authorizationCode);
        form.add("client_id", credentials.clientId());
        form.add("client_secret", credentials.clientSecret());
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(credentials.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);
        } catch (RuntimeException exception) {
            throw new BadCredentialsException("Google authorization code could not be verified", exception);
        }
        if (tokenResponse == null || tokenResponse.idToken() == null || tokenResponse.idToken().isBlank()) {
            throw new BadCredentialsException("Google authorization code could not be verified");
        }

        Jwt identity;
        try {
            identity = createDecoder(credentials.clientId()).decode(tokenResponse.idToken());
        } catch (JwtException exception) {
            throw new BadCredentialsException("Google identity token is invalid", exception);
        }
        String actualNonce = identity.getClaimAsString("nonce");
        if (actualNonce == null || !MessageDigest.isEqual(
                expectedNonce.getBytes(StandardCharsets.UTF_8), actualNonce.getBytes(StandardCharsets.UTF_8))) {
            throw new BadCredentialsException("Google authorization nonce is invalid");
        }
        String email = verifiedEmail(identity);
        String completionCode = randomToken();
        removeExpiredCompletions();
        pendingIdentities.put(completionCode, new PendingIdentity(
                email,
                namePart(identity, "given_name", true),
                namePart(identity, "family_name", false),
                Instant.now().plus(COMPLETION_CODE_VALIDITY)));
        return completionCode;
    }

    @Transactional
    public GoogleAuthResponse completeSignIn(String completionCode, String phoneNumber) throws BusinessException {
        if (completionCode == null || completionCode.isBlank()) {
            throw new BadCredentialsException("Google authorization has expired");
        }
        removeExpiredCompletions();
        PendingIdentity identity = pendingIdentities.get(completionCode);
        if (identity == null) {
            throw new BadCredentialsException("Google authorization has expired");
        }

        if (appUserRepository.existsByUsername(identity.email())) {
            LoginResponse session = authService.loginWithGoogle(identity.email());
            pendingIdentities.remove(completionCode, identity);
            return GoogleAuthResponse.authenticated(session);
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return GoogleAuthResponse.registrationRequired(
                    identity.email(), identity.firstName(), identity.lastName(), completionCode);
        }
        if (!phoneNumber.matches("^[0-9]{9}$")) {
            throw new BusinessException("Phone number must contain 9 digits");
        }

        Client client = new Client(Integer.valueOf(phoneNumber), randomToken(),
                identity.firstName(), identity.lastName(), identity.email());
        clientService.registerClient(client);
        LoginResponse session = authService.loginWithGoogle(identity.email());
        pendingIdentities.remove(completionCode, identity);
        return GoogleAuthResponse.authenticated(session);
    }

    private GoogleCredentials credentials() throws BusinessException {
        GoogleCredentials current = cachedCredentials;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (cachedCredentials != null) {
                return cachedCredentials;
            }
            String clientId = configuredClientId;
            String clientSecret = configuredClientSecret;
            String tokenUri = GOOGLE_TOKEN_URI;
            if ((!clientId.isEmpty() && !clientSecret.isEmpty())) {
                cachedCredentials = new GoogleCredentials(clientId, clientSecret, tokenUri);
                return cachedCredentials;
            }
            if (credentialsFile.isEmpty()) {
                throw new BusinessException("Google OAuth client credentials are not configured");
            }
            try {
                JsonNode root = objectMapper.readTree(Files.readString(Path.of(credentialsFile)));
                JsonNode webCredentials = root.has("web") ? root.get("web")
                        : root.has("installed") ? root.get("installed") : root;
                clientId = webCredentials.path("client_id").asText("");
                clientSecret = webCredentials.path("client_secret").asText("");
                tokenUri = webCredentials.path("token_uri").asText(GOOGLE_TOKEN_URI);
                if (clientId.isBlank() || clientSecret.isBlank()) {
                    throw new BusinessException("Google OAuth client credentials file is invalid");
                }
                cachedCredentials = new GoogleCredentials(clientId, clientSecret, tokenUri);
                return cachedCredentials;
            } catch (IOException | RuntimeException exception) {
                throw new BusinessException("Google OAuth client credentials file could not be read");
            }
        }
    }

    private String verifiedEmail(Jwt identity) {
        String email = identity.getClaimAsString("email");
        if (!Boolean.TRUE.equals(identity.getClaim("email_verified")) || email == null || email.isBlank()) {
            throw new BadCredentialsException("Google account email is not verified");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private JwtDecoder createDecoder(String expectedClientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        OAuth2TokenValidator<Jwt> googleClaimsValidator = jwt -> {
            String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
            boolean validIssuer = "https://accounts.google.com".equals(issuer)
                    || "accounts.google.com".equals(issuer);
            boolean validAudience = jwt.getAudience().contains(expectedClientId);
            return validIssuer && validAudience
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Invalid Google token issuer or audience", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(), googleClaimsValidator));
        return decoder;
    }

    private void removeExpiredCompletions() {
        Instant now = Instant.now();
        pendingIdentities.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String namePart(Jwt identity, String claimName, boolean firstName) {
        String value = identity.getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            String fullName = identity.getClaimAsString("name");
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                value = firstName ? parts[0] : (parts.length > 1 ? parts[1] : "Cliente");
            } else {
                value = firstName ? "Cliente" : "Google";
            }
        }
        value = value.trim();
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    private record GoogleCredentials(String clientId, String clientSecret, String tokenUri) {
    }

    private record PendingIdentity(String email, String firstName, String lastName, Instant expiresAt) {
    }

    private record GoogleTokenResponse(@JsonProperty("id_token") String idToken) {
    }
}
