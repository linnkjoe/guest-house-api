package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.EmailAvailability;
import com.mabjaiainn.proj.DTO.EmailVerificationRequest;
import com.mabjaiainn.proj.DTO.GoogleAuthResponse;
import com.mabjaiainn.proj.DTO.GoogleOAuthCompleteRequest;
import com.mabjaiainn.proj.DTO.RegisterRequest;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mabjaiainn.proj.DTO.LoginRequest;
import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.service.AuthService;
import com.mabjaiainn.proj.service.EmailVerificationService;
import com.mabjaiainn.proj.service.GoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String GOOGLE_STATE_COOKIE = "mabjaia-google-state";
    private static final String GOOGLE_NONCE_COOKIE = "mabjaia-google-nonce";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final EmailVerificationService emailVerificationService;
    private final AppUserRepository appUserRepository;
    private final String googleRedirectUri;
    private final String frontendRedirectUri;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService,
            EmailVerificationService emailVerificationService,
            AppUserRepository appUserRepository,
            @Value("${app.security.google.redirect-uri:http://localhost:5173/backend/auth/google/callback}") String googleRedirectUri,
            @Value("${app.security.google.frontend-redirect-uri:http://localhost:5173/}") String frontendRedirectUri) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.emailVerificationService = emailVerificationService;
        this.appUserRepository = appUserRepository;
        this.googleRedirectUri = googleRedirectUri;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    // Publico (liberado em SpringSecurityConfiguration): qualquer pessoa pode tentar autenticar-se.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/google/start")
    public void startGoogleAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws IOException, BusinessException {
        String state = randomToken();
        String nonce = randomToken();
        boolean secureCookie = request.isSecure()
                || "https".equalsIgnoreCase(java.net.URI.create(googleRedirectUri).getScheme());
        addOAuthCookie(response, GOOGLE_STATE_COOKIE, state, secureCookie);
        addOAuthCookie(response, GOOGLE_NONCE_COOKIE, nonce, secureCookie);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.sendRedirect(googleAuthService.authorizationUrl(state, nonce));
    }

    @GetMapping("/google/callback")
    public void googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String expectedState = cookieValue(request, GOOGLE_STATE_COOKIE);
        String nonce = cookieValue(request, GOOGLE_NONCE_COOKIE);
        boolean secureCookie = request.isSecure()
                || "https".equalsIgnoreCase(java.net.URI.create(googleRedirectUri).getScheme());
        clearOAuthCookies(response, secureCookie);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (error != null || code == null || state == null || expectedState == null || nonce == null
                || !MessageDigest.isEqual(state.getBytes(StandardCharsets.UTF_8),
                        expectedState.getBytes(StandardCharsets.UTF_8))) {
            response.sendRedirect(frontendRedirectUri + "?google_error=authorization");
            return;
        }
        try {
            String completionCode = googleAuthService.completeAuthorization(code, nonce);
            response.sendRedirect(frontendRedirectUri + "?google_code="
                    + java.net.URLEncoder.encode(completionCode, StandardCharsets.UTF_8));
        } catch (org.springframework.security.authentication.BadCredentialsException | BusinessException exception) {
            response.sendRedirect(frontendRedirectUri + "?google_error=authorization");
        }
    }

    @PostMapping("/google/complete")
    public GoogleAuthResponse completeGoogleAuthentication(
            @Valid @RequestBody GoogleOAuthCompleteRequest request) throws BusinessException {
        return googleAuthService.completeSignIn(request.code(), request.phoneNumber());
    }

    // Publico: inicia o auto cadastro enviando um código para o e-mail indicado.
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request)
            throws BusinessException {
        if (!request.clientPassword().equals(request.confirmClientPassword())) {
            throw new BusinessException("Passwords do not match");
        }
        emailVerificationService.sendVerificationCode(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/verify")
    public LoginResponse verifyRegistration(@Valid @RequestBody EmailVerificationRequest request)
            throws BusinessException {
        return emailVerificationService.verify(request.email(), request.code());
    }

    // Publico: permite validar o e-mail ao sair do campo, antes de enviar o formulario.
    @GetMapping("/email-available")
    public EmailAvailability emailAvailable(@RequestParam String email) {
        return new EmailAvailability(!appUserRepository.existsByUsername(email.trim().toLowerCase()));
    }

    private void addOAuthCookie(HttpServletResponse response, String name, String value, boolean secure) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(cookiePath())
                .maxAge(Duration.ofMinutes(5))
                .build().toString());
    }

    private void clearOAuthCookies(HttpServletResponse response, boolean secure) {
        for (String cookie : new String[]{GOOGLE_STATE_COOKIE, GOOGLE_NONCE_COOKIE}) {
            response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(cookie, "")
                    .httpOnly(true)
                    .secure(secure)
                    .sameSite("Lax")
                    .path(cookiePath())
                    .maxAge(Duration.ZERO)
                    .build().toString());
        }
    }

    private String cookiePath() {
        String path = java.net.URI.create(googleRedirectUri).getPath();
        return path == null || path.isBlank() ? "/auth/google/callback" : path;
    }

    private String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
