package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.LoginResponse;
import com.mabjaiainn.proj.DTO.RegisterRequest;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    @Test
    void createsAccountOnlyAfterCodeMatches() throws Exception {
        AppUserRepository users = mock(AppUserRepository.class);
        ClientService clients = mock(ClientService.class);
        AuthService auth = mock(AuthService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        when(senderProvider.getIfAvailable()).thenReturn(sender);
        when(encoder.encode("a-strong-password")).thenReturn("encoded-password");
        when(users.existsByUsername("user@example.com")).thenReturn(false);
        LoginResponse session = new LoginResponse("token", "Bearer", 3600, "user@example.com",
                "Test User", "CLIENTE", 12L, false);
        when(auth.loginWithGoogle("user@example.com")).thenReturn(session);
        EmailVerificationService service = new EmailVerificationService(users, clients, auth, encoder,
                senderProvider, "smtp.example.com", "mailer", "password", "mailer@example.com");
        RegisterRequest request = new RegisterRequest("Test", "User", "user@example.com", 912345678,
                "a-strong-password", "a-strong-password");

        service.sendVerificationCode(request);
        org.mockito.ArgumentCaptor<SimpleMailMessage> mailCaptor =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(mailCaptor.capture());
        String code = mailCaptor.getValue().getText().replaceAll("(?s).*?([0-9]{6}).*", "$1");
        assertEquals(6, code.length());
        verify(clients, never()).registerClientWithEncodedPassword(any(), anyString());

        assertEquals(session, service.verify("USER@example.com", code));
        verify(clients).registerClientWithEncodedPassword(any(), org.mockito.ArgumentMatchers.eq("encoded-password"));
    }

    @Test
    void rejectsVerificationWhenMailIsNotConfigured() {
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        when(senderProvider.getIfAvailable()).thenReturn(null);
        EmailVerificationService service = new EmailVerificationService(
                mock(AppUserRepository.class),
                mock(ClientService.class),
                mock(AuthService.class),
                mock(PasswordEncoder.class),
                senderProvider,
                "",
                "",
                "",
                "");

        assertThrows(BusinessException.class, () -> service.sendVerificationCode(
                new RegisterRequest("Test", "User", "user@example.com", 912345678,
                        "a-strong-password", "a-strong-password")));
    }
}
