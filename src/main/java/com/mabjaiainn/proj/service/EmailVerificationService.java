package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.DTO.RegisterRequest;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final Duration CODE_VALIDITY = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository appUserRepository;
    private final ClientService clientService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String mailHost;
    private final String mailUsername;
    private final String mailPassword;
    private final String mailFrom;
    private final Map<String, PendingRegistration> registrations = new ConcurrentHashMap<>();

    public EmailVerificationService(
            AppUserRepository appUserRepository,
            ClientService clientService,
            AuthService authService,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${app.mail.from:}") String mailFrom) {
        this.appUserRepository = appUserRepository;
        this.clientService = clientService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailHost = mailHost == null ? "" : mailHost.trim();
        this.mailUsername = mailUsername == null ? "" : mailUsername.trim();
        this.mailPassword = mailPassword == null ? "" : mailPassword.trim();
        this.mailFrom = mailFrom == null ? "" : mailFrom.trim();
    }

    public void sendVerificationCode(RegisterRequest request) throws BusinessException {
        ensureMailConfigured();
        String email = normalizeEmail(request.clientEmail());
        if (appUserRepository.existsByUsername(email)) {
            throw new BusinessException("Email already registered");
        }
        PendingRegistration current = registrations.get(email);
        Instant now = Instant.now();
        registrations.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (current != null && current.expiresAt().isAfter(now)
                && current.createdAt().plus(RESEND_DELAY).isAfter(now)) {
            throw new BusinessException("Verification code was recently sent");
        }

        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        PendingRegistration pending = new PendingRegistration(
                request.clientFirstName().trim(),
                request.clientLastName().trim(),
                request.phoneNumber(),
                passwordEncoder.encode(request.clientPassword()),
                hash(code),
                now,
                now.plus(CODE_VALIDITY),
                0);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Confirme o seu e-mail - Mabjaia Inn");
        message.setText("O seu código de confirmação é " + code
                + ". O código expira em 10 minutos. Se não pediu a criação de uma conta, ignore esta mensagem.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOGGER.error("Failed to send email verification through SMTP host '{}'", mailHost, exception);
            throw new BusinessException("Could not send email verification code");
        }
        registrations.put(email, pending);
    }

    @Transactional
    public synchronized LoginResponse verify(String rawEmail, String code) throws BusinessException {
        String email = normalizeEmail(rawEmail);
        PendingRegistration pending = registrations.get(email);
        Instant now = Instant.now();
        if (pending == null || !pending.expiresAt().isAfter(now)) {
            registrations.remove(email);
            throw new BusinessException("Email verification code is invalid or expired");
        }
        if (pending.attempts() >= MAX_ATTEMPTS) {
            registrations.remove(email);
            throw new BusinessException("Too many email verification attempts");
        }
        if (!MessageDigest.isEqual(pending.codeHash().getBytes(StandardCharsets.US_ASCII),
                hash(code).getBytes(StandardCharsets.US_ASCII))) {
            int attempts = pending.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                registrations.remove(email, pending);
                throw new BusinessException("Too many email verification attempts");
            }
            registrations.replace(email, pending, pending.withAttempts(attempts));
            throw new BusinessException("Email verification code is invalid or expired");
        }
        if (appUserRepository.existsByUsername(email)) {
            registrations.remove(email, pending);
            throw new BusinessException("Email already registered");
        }

        Client client = new Client(pending.phoneNumber(), pending.encodedPassword(),
                pending.firstName(), pending.lastName(), email);
        clientService.registerClientWithEncodedPassword(client, pending.encodedPassword());
        LoginResponse session = authService.loginWithGoogle(email);
        registrations.remove(email, pending);
        return session;
    }

    private void ensureMailConfigured() throws BusinessException {
        if (mailSender == null || mailHost.isBlank() || mailUsername.isBlank()
                || mailPassword.isBlank() || mailFrom.isBlank()) {
            throw new BusinessException("Email verification is not configured");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hash(String value) throws BusinessException {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException("Email verification is unavailable");
        }
    }

    private record PendingRegistration(
            String firstName,
            String lastName,
            Integer phoneNumber,
            String encodedPassword,
            String codeHash,
            Instant createdAt,
            Instant expiresAt,
            int attempts) {

        private PendingRegistration withAttempts(int count) {
            return new PendingRegistration(firstName, lastName, phoneNumber, encodedPassword,
                    codeHash, createdAt, expiresAt, count);
        }
    }
}
