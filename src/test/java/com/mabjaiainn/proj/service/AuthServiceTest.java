package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.AuthUserDetails;
import com.mabjaiainn.proj.DTO.LoginRequest;
import com.mabjaiainn.proj.entity.Role;
import com.mabjaiainn.proj.repository.AppUserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode("not-a-real-password")).thenReturn("dummy-hash");
        authService = new AuthService(appUserRepository, passwordEncoder, jwtEncoder, 480);
    }

    @Test
    void authenticatesUsingOnlyTheLightweightAccountProjection() {
        when(appUserRepository.findAuthDetailsByUsername("admin"))
                .thenReturn(Optional.of(new AuthUserDetails("admin", "stored-hash", "Admin", Role.ADMIN, null, true, false)));
        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed-token")
                        .header("alg", "HS256")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());

        var response = authService.login(new LoginRequest(" Admin ", "correct-password"));

        assertEquals("admin", response.username());
        assertEquals("ADMIN", response.role());
        assertEquals(false, response.mustChangePassword());
        assertEquals("signed-token", response.token());
        verify(appUserRepository).findAuthDetailsByUsername("admin");
    }

    @Test
    void invalidCredentialsAreStillCheckedAgainstTheDummyHash() {
        when(appUserRepository.findAuthDetailsByUsername("unknown")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("wrong-password", "dummy-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("unknown", "wrong-password")));
    }

    @Test
    void disabledManagerCannotAuthenticateEvenWithTheCorrectPassword() {
        when(appUserRepository.findAuthDetailsByUsername("manager"))
                .thenReturn(Optional.of(new AuthUserDetails(
                        "manager", "stored-hash", "Manager", Role.GESTOR, null, false, false)));
        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("manager", "correct-password")));
    }

    @Test
    void firstLoginManagerFlagIsReturnedForForcedPasswordChange() {
        when(appUserRepository.findAuthDetailsByUsername("manager"))
                .thenReturn(Optional.of(new AuthUserDetails(
                        "manager", "stored-hash", "Manager", Role.GESTOR, null, true, true)));
        when(passwordEncoder.matches("temporary-password", "stored-hash")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed-token")
                        .header("alg", "HS256")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build());

        var response = authService.login(new LoginRequest("manager", "temporary-password"));

        assertEquals(true, response.mustChangePassword());
    }
}
